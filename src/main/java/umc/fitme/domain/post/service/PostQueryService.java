package umc.fitme.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.interest.repository.PostInterestRepository;
import umc.fitme.domain.interest.repository.UserInterestRepository;
import umc.fitme.domain.post.converter.PostConverter;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.entity.ViewHistory;
import umc.fitme.domain.post.enums.ClosingSoonSort;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.post.repository.ViewHistoryRepository;
import umc.fitme.domain.post.util.UniversityTypeResolver;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.exception.UserException;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.domain.user.repository.UserSaveRepository;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private static final int COLD_START_SIZE = 5;
    private static final int MAX_CANDIDATE_SIZE = 500;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?");

    // 인기 공고는 공모전과 장학금을 3:2 비율로 노출한다.
    private static final int POPULAR_CONTEST_RATIO = 3;
    private static final int POPULAR_SCHOLARSHIP_RATIO = 2;
    private static final int POPULAR_RATIO_SUM = POPULAR_CONTEST_RATIO + POPULAR_SCHOLARSHIP_RATIO;
    private static final int POPULAR_DEFAULT_SIZE = 8;

    private final PostRepository postRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserInterestRepository userInterestRepository;
    private final PostInterestRepository postInterestRepository;
    private final UserSaveRepository userSaveRepository;
    private final PostSummaryService postSummaryService;
    private final UniversityTypeResolver universityTypeResolver;


    /**
     * 실시간 인기 공고를 공모전:장학금 = 3:2 비율로 구성한다.
     * 한쪽 공고 수가 배정량에 못 미치면 부족한 만큼 다른 종류로 채워 요청한 개수를 유지한다.
     * userId가 없으면(비로그인) 찜 여부는 모두 false로 내려간다.
     */
    public PostResponseDTO.PopularPostListDTO getPopularPosts(Long userId, Long cursor, Integer size) {
        int totalSize = (size == null || size <= 0) ? POPULAR_DEFAULT_SIZE : size;

        // 후보를 요청 개수만큼 넉넉히 뽑아두고, 한쪽이 부족할 때 나머지로 보충한다.
        PageRequest candidateRequest = PageRequest.of(0, totalSize);
        List<Post> contests = postRepository.findRandomPostsByType(PostType.CONTEST, candidateRequest);
        List<Post> scholarships = postRepository.findRandomPostsByType(PostType.SCHOLARSHIP, candidateRequest);

        // 3:2로 나눌 때 나머지는 비율이 큰 공모전 쪽에 준다. (size=8 -> 공모전 5, 장학금 3)
        int contestQuota = (int) Math.ceil((double) totalSize * POPULAR_CONTEST_RATIO / POPULAR_RATIO_SUM);
        int contestTake = Math.min(contestQuota, contests.size());
        int scholarshipTake = Math.min(totalSize - contestQuota, scholarships.size());

        int shortage = totalSize - contestTake - scholarshipTake;
        if (shortage > 0) {
            int extraScholarship = Math.min(shortage, scholarships.size() - scholarshipTake);
            scholarshipTake += extraScholarship;
            shortage -= extraScholarship;
        }
        if (shortage > 0) {
            contestTake += Math.min(shortage, contests.size() - contestTake);
        }

        List<Post> popularPosts = new ArrayList<>();
        popularPosts.addAll(contests.subList(0, contestTake));
        popularPosts.addAll(scholarships.subList(0, scholarshipTake));
        // 종류별로 묶여 보이지 않도록 섞어서 내려준다.
        Collections.shuffle(popularPosts);

        // 로그인 사용자면 찜한 공고 id를 모아 카드에 찜 상태를 표시한다.
        Set<Long> savedPostIds = userId == null
                ? Set.of()
                : userSaveRepository.findSavedPostIdsByUserId(userId);

        // 커서 페이징은 아직 적용하지 않는다. (기존 동작 유지)
        return PostConverter.toPopularPostListDTO(popularPosts, false, null, savedPostIds);
    }


    public PostResponseDTO.PostPreviewListDTO getRecentPosts(Long userId, Integer page, Integer size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.clamp(size, 1, 100);
        Page<ViewHistory> viewHistories = viewHistoryRepository.findAllByUserIdOrderByViewedAtDesc(
                userId,
                PageRequest.of(normalizedPage, normalizedSize)
        );

        List<Post> posts = viewHistories.getContent().stream()
                .map(ViewHistory::getPost)
                .toList();

        // 회원 이름 조회
        String name = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND)).getName();

        Set<Long> savedPostIds = userSaveRepository.findSavedPostIdsByUserId(userId);
        return PostConverter.toPostPreviewListDTO(name, posts, viewHistories.hasNext(), null, savedPostIds);
    }

    /**
     * 공고 상세 조회가 구현되면 상세 조회 성공 직후 호출한다.
     * 같은 사용자가 같은 공고를 다시 조회한 경우에는 이력을 중복 생성하지 않고 조회 시각만 갱신한다.
     */
    @Transactional
    public void recordViewHistory(Long userId, Long postId) {
        LocalDateTime now = LocalDateTime.now();

        viewHistoryRepository.findByUserIdAndPostId(userId, postId)
                .ifPresentOrElse(
                        history -> history.updateViewedAt(now),
                        () -> {
                            Post post = postRepository.findById(postId)
                                    .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));
                            User userReference = userRepository.findById(userId)
                                    .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));
                            viewHistoryRepository.save(ViewHistory.builder()
                                    .user(userReference)
                                    .post(post)
                                    .viewedAt(now)
                                    .build());
                        }
                );
    }

    /**
     * 로그인 연결 전에는 userId를 요청 파라미터로 받아 테스트한다.
     * 로그인 기능 병합 후에는 인증된 사용자 ID를 전달하도록 교체한다.
     */
    @Transactional
    public PostResponseDTO.PostDetailDTO getPostDetail(Long userId, Long postId) {
        return getPostDetail(userId, postId, null);
    }

    /**
     * 장학금 상세 전용 조회입니다.
     * URL은 장학금 경로지만 다른 타입의 공고 ID가 들어오는 경우에는 조회를 허용하지 않습니다.
     */
    @Transactional
    public PostResponseDTO.PostDetailDTO getScholarshipPostDetail(Long userId, Long postId) {
        return getPostDetail(userId, postId, PostType.SCHOLARSHIP);
    }

    /**
     * 공모전 공고 상세 화면 진입 API.
     * 장학금 상세와 동일하게 조회 수 및 최근 조회 이력을 갱신한다.
     * URL은 공모전 경로지만 다른 타입의 공고 ID가 들어오는 경우에는 조회를 허용하지 않는다.
     */
    @Transactional
    public PostResponseDTO.PostDetailDTO getContestPostDetail(Long userId, Long postId) {
        return getPostDetail(userId, postId, PostType.CONTEST);
    }

    private PostResponseDTO.PostDetailDTO getPostDetail(
            Long userId,
            Long postId,
            PostType expectedPostType
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));

        if (expectedPostType == null || post.getPostType() != expectedPostType) {
            throw new ProjectException(GeneralErrorCode.NOT_FOUND);
        }

        post.incrementViewCount();
        recordViewHistory(userId, postId);

        // 상세 조회로 변경된 조회수·최근 조회 시간을 즉시 DB에 반영한다.
        postRepository.flush();

        // 찜 여부는 목록과 동일하게 한 번의 조회로 확인한다.
        Set<Long> savedPostIds = userSaveRepository.findSavedPostIdsByUserId(userId);
        boolean isSaved = savedPostIds != null && savedPostIds.contains(postId);

        // 캐싱된 일반 AI 요약 + 유저 프로필(이름/관심분야)을 템플릿으로 결합한다 (AI 재호출 없음).
        String userName = userRepository.findById(userId).map(User::getName).orElse(null);
        Set<String> interestFields = new HashSet<>(userInterestRepository.findInterestNamesByUserId(userId));
        String personalizedSummary = postSummaryService.buildPersonalizedSummary(post, userName, interestFields);

        return PostConverter.toPostDetailDTO(post, isSaved, personalizedSummary);
    }


    /**
     * 실제 로그인 사용자용 진입점입니다.
     * 사용자 상세정보와 관심분야 기능이 병합되면 인증된 userId를 전달합니다.
     */
    public List<PostResponseDTO.PostPreviewDTO> getClosingSoonPosts(
            Long userId,
            PostType postType,
            ClosingSoonSort sort,
            Integer size
    ) {
        // 현재 홈 화면 정책은 FIT 정렬만 제공한다.
        // FIT: 조건 일치 수가 높은 순, 동률이면 마감일이 가까운 순.
        // 실제 사용자 정보와 찜한 공고 ID를 한 번에 조회한다.
        UserFitProfile profile = createUserFitProfile(userId);
        Set<Long> savedPostIds = userSaveRepository.findSavedPostIdsByUserId(userId);

        // 사용자 맞춤 정보가 없으면 인기 공고를 기본값으로 반환한다.
        if (!hasAnyFitInformation(profile)) {
            return getColdStartPosts(postType, savedPostIds);
        }
        return getClosingSoonPosts(profile, postType, size, savedPostIds);
    }

    // 기존 호출부와 테스트 코드 호환을 위한 FIT 기본 진입점이다.
    public List<PostResponseDTO.PostPreviewDTO> getClosingSoonPosts(
            Long userId,
            PostType postType,
            Integer size
    ) {
        return getClosingSoonPosts(userId, postType, ClosingSoonSort.FIT, size);
    }

    /**
     * 사용자 정보 공급원과 공고 매칭 로직을 분리한 진입점입니다.
     * 로컬 테스트에서는 더미 프로필을, 실제 기능에서는 DB 프로필을 전달합니다.
     */
    public List<PostResponseDTO.PostPreviewDTO> getClosingSoonPosts(
            UserFitProfile profile,
            PostType postType,
            Integer size
    ) {
        return getClosingSoonPosts(profile, postType, size, Set.of());
    }

    private List<PostResponseDTO.PostPreviewDTO> getClosingSoonPosts(
            UserFitProfile profile,
            PostType postType,
            Integer size,
            Set<Long> savedPostIds
    ) {
        // 요청 개수를 1~100개로 제한한다.
        int normalizedSize = Math.clamp(size, 1, 100);
        int candidateSize = Math.min(
                Math.max(normalizedSize * 10, 100),
                MAX_CANDIDATE_SIZE
        );

        // 마감되지 않은 공고를 마감일이 가까운 순서로 조회한다.
        List<Post> candidates = postRepository.findClosingSoonPosts(
                postType,
                PageRequest.of(0, candidateSize)
        );

        // 공고별 관심 분야를 한 번의 쿼리로 조회해 N+1 문제를 방지한다.
        Map<Long, Set<String>> postInterests = loadPostInterests(candidates);

        // 사용자 조건에 맞는 공고만 남긴다. 자격 판정은 scorePost가 하고,
        // 남은 공고는 마감이 급한 순서로 보여 준다. 일치 조건 수는 같은 날짜끼리 순서를 정할 때만 쓴다.
        List<ScoredPost> matchedPosts = candidates.stream()
                .map(post -> scorePost(post, postInterests.getOrDefault(post.getId(), Set.of()), profile))
                .filter(ScoredPost::eligible)
                .sorted(Comparator
                        .comparing((ScoredPost scored) -> scored.post().getApplyEndAt())
                        .thenComparing(Comparator.comparingInt(ScoredPost::matchCount).reversed())
                        .thenComparing(scored -> scored.post().getId()))
                .limit(normalizedSize)
                .toList();

        // 맞춤 공고가 없으면 찜 횟수가 높은 공고 5개를 대신 반환한다.
        if (matchedPosts.isEmpty()) {
            return getColdStartPosts(postType, savedPostIds);
        }

        // 각 공고의 찜 여부와 D-Day를 응답 DTO에 포함한다.
        return matchedPosts.stream()
                .map(ScoredPost::post)
                .map(post -> PostConverter.toPostPreviewDTO(
                        post,
                        savedPostIds.contains(post.getId())
                ))
                .toList();
    }

    private UserFitProfile createUserFitProfile(Long userId) {
        // 사용자 상세 정보와 관심 분야를 조합해 매칭용 프로필을 만든다.
        UserDetail userDetail = userDetailRepository.findByUserId(userId).orElse(null);
        Set<String> interests = new HashSet<>(
                userInterestRepository.findInterestNamesByUserId(userId)
        );

        return new UserFitProfile(
                userDetail == null ? null : userDetail.getGpa().doubleValue(),
                userDetail == null ? null : userDetail.getIncomeBracket(),
                userDetail == null ? null : userDetail.getRegion(),
                userDetail == null ? null : userDetail.getUniversityName(),
                interests
        );
    }

    private boolean hasAnyFitInformation(UserFitProfile profile) {
        return profile.gpa() != null
                || profile.incomeBracket() != null
                || hasText(profile.region())
                || hasText(profile.university())
                || !profile.interestFields().isEmpty();
    }

    private List<PostResponseDTO.PostPreviewDTO> getColdStartPosts(
            PostType postType,
            Set<Long> savedPostIds
    ) {
        return postRepository.findPopularPostsBySavedCount(
                        postType,
                        PageRequest.of(0, COLD_START_SIZE)
                ).stream()
                .map(post -> PostConverter.toPostPreviewDTO(
                        post,
                        savedPostIds.contains(post.getId())
                ))
                .toList();
    }

    private Map<Long, Set<String>> loadPostInterests(List<Post> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Set<String>> result = new HashMap<>();

        for (Object[] row : postInterestRepository.findPostInterestNamesByPostIds(postIds)) {
            Long postId = (Long) row[0];
            String interestName = normalize((String) row[1]);
            result.computeIfAbsent(postId, ignored -> new HashSet<>()).add(interestName);
        }
        return result;
    }

    private ScoredPost scorePost(
            Post post,
            Set<String> postInterests,
            UserFitProfile profile
    ) {
        int matchCount = 0;

        // 장학금은 학점, 소득구간, 거주지역, 소속 대학 조건을 모두 확인한다.
        if (post instanceof Scholarship scholarship) {
            ConditionMatch gpaMatch = matchGpa(scholarship.getGradeRequirement(), profile.gpa());
            if (!gpaMatch.matches()) {
                return ScoredPost.ineligible(post);
            }
            matchCount += gpaMatch.matchScore();

            ConditionMatch incomeMatch = matchIncome(
                    scholarship.getIncomeRequirement(),
                    profile.incomeBracket()
            );
            if (!incomeMatch.matches()) {
                return ScoredPost.ineligible(post);
            }
            matchCount += incomeMatch.matchScore();

            ConditionMatch regionMatch = matchRegion(
                    scholarship.getRegionRequirement(),
                    profile.region()
            );
            if (!regionMatch.matches()) {
                return ScoredPost.ineligible(post);
            }
            matchCount += regionMatch.matchScore();

            // 특정 대학 공고는 사용자 대학이 일치해야 하며, 전국/제한 없음은 무조건 통과한다.
            ConditionMatch universityMatch = matchUniversity(
                    scholarship.getUniversityRequirement(),
                    profile.university()
            );
            if (!universityMatch.matches()) {
                return ScoredPost.ineligible(post);
            }
            matchCount += universityMatch.matchScore();
        }

        // 사용자와 공고의 관심 분야가 하나라도 겹치는지 확인한다.
        ConditionMatch interestMatch = matchInterest(postInterests, profile.interestFields());
        if (!interestMatch.matches()) {
            return ScoredPost.ineligible(post);
        }
        matchCount += interestMatch.matchScore();

        return new ScoredPost(post, matchCount, true);
    }

    private ConditionMatch matchGpa(String requirement, Double userGpa) {
        if (isUnrestricted(requirement)) {
            return ConditionMatch.unrestricted();
        }

        List<Double> numbers = extractNumbers(requirement);
        if (numbers.isEmpty()) {
            return ConditionMatch.unrestricted();
        }

        return ConditionMatch.restricted(userGpa != null && userGpa >= numbers.getFirst());
    }

    private ConditionMatch matchIncome(String requirement, Integer userIncomeBracket) {
        if (isUnrestricted(requirement)) {
            return ConditionMatch.unrestricted();
        }

        List<Double> numbers = extractNumbers(requirement);
        if (numbers.isEmpty()) {
            return ConditionMatch.unrestricted();
        }
        if (userIncomeBracket == null) {
            return ConditionMatch.restricted(false);
        }

        String normalizedRequirement = normalize(requirement);
        boolean matches;
        if (numbers.size() >= 2) {
            double min = Math.min(numbers.get(0), numbers.get(1));
            double max = Math.max(numbers.get(0), numbers.get(1));
            matches = userIncomeBracket >= min && userIncomeBracket <= max;
        } else if (normalizedRequirement.contains("이상")) {
            matches = userIncomeBracket >= numbers.getFirst();
        } else if (normalizedRequirement.contains("이하")) {
            matches = userIncomeBracket <= numbers.getFirst();
        } else {
            matches = userIncomeBracket == numbers.getFirst().intValue();
        }
        return ConditionMatch.restricted(matches);
    }

    /**
     * 지역 조건은 판정하지 않고 모두 통과시킨다.
     * <p>
     * 공고의 지역 조건은 문장으로 적혀 있고(예: "○ 공고일 기준 본인 또는 부모가 주민등록초본 상
     * 강원특별자치도에 1년 이상 계속해서 주소지를 두고 있는 자") 사용자 지역은 "서울" 같은 시·도
     * 한 단어여서, 문자열 비교로는 맞출 수 없다. 그대로 두면 실제로는 지원할 수 있는 공고까지
     * 걸러져서 목록에서 사라진다. 대학 조건과 같은 원칙으로, 판정하지 못하는 조건은 가리지 않는다.
     * <p>
     * 사용자 지역 입력이 표준화되면(온보딩 협의 필요) 그때 실제 판정을 넣는다.
     */
    private ConditionMatch matchRegion(String requirement, String userRegion) {
        return ConditionMatch.unrestricted();
    }

    /**
     * 공고의 대학 조건과 사용자의 소속 대학을 맞춰 본다.
     * <p>
     * 공고에는 대학 구분이(예: {@code 4년제(5~6년제포함)전문대(2~3년제)}),
     * 사용자에게는 대학 이름이(예: {@code 가천대학교}) 있으므로
     * 이름을 구분으로 바꾼 뒤 비교한다.
     * <p>
     * 판정할 수 없는 경우는 모두 통과시킨다. 우리가 알아보지 못한 것 때문에
     * 사용자가 지원할 수 있는 공고를 가리는 편보다, 못 거른 공고가 섞이는 편이 낫다.
     */
    private ConditionMatch matchUniversity(String requirement, String userUniversity) {
        if (isUnrestricted(requirement)) {
            return ConditionMatch.unrestricted();
        }
        // '도내 대학 해외교환 장학생'처럼 문장으로 적힌 조건은 구분으로 판정할 수 없다.
        if (!universityTypeResolver.containsTypeKeyword(requirement)) {
            return ConditionMatch.unrestricted();
        }
        if (!hasText(userUniversity)) {
            return ConditionMatch.unrestricted();
        }

        String userType = universityTypeResolver.resolveType(userUniversity);
        if (userType == null) {
            // 대학 목록에 없는 학교는 구분을 알 수 없으므로 거르지 않는다.
            return ConditionMatch.unrestricted();
        }

        return ConditionMatch.restricted(normalize(requirement).contains(normalize(userType)));
    }

    private ConditionMatch matchInterest(
            Set<String> postInterests,
            Set<String> userInterests
    ) {
        if (postInterests.isEmpty()) {
            return ConditionMatch.unrestricted();
        }
        if (userInterests.isEmpty()) {
            return ConditionMatch.restricted(false);
        }

        Set<String> normalizedUserInterests = new HashSet<>();
        for (String interest : userInterests) {
            normalizedUserInterests.add(normalize(interest));
        }

        boolean matches = postInterests.stream().anyMatch(normalizedUserInterests::contains);
        return ConditionMatch.restricted(matches);
    }

    private boolean isUnrestricted(String value) {
        // 제한 문구가 없거나 '제한 없음'에 해당하면 모든 사용자가 통과한다.
        if (!hasText(value)) {
            return true;
        }
        String normalizedValue = normalize(value);
        return normalizedValue.contains("제한없음")
                || normalizedValue.contains("기준없음")
                || normalizedValue.contains("해당없음")
                || normalizedValue.contains("무관")
                || normalizedValue.contains("전국");
    }

    private List<Double> extractNumbers(String value) {
        List<Double> numbers = new ArrayList<>();
        if (value == null) {
            return numbers;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(value);
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        return numbers;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ConditionMatch(boolean matches, int matchScore) {
        private static ConditionMatch unrestricted() {
            return new ConditionMatch(true, 0);
        }

        private static ConditionMatch restricted(boolean matches) {
            return new ConditionMatch(matches, matches ? 1 : 0);
        }
    }

    private record ScoredPost(Post post, int matchCount, boolean eligible) {
        private static ScoredPost ineligible(Post post) {
            return new ScoredPost(post, 0, false);
        }
    }

}
