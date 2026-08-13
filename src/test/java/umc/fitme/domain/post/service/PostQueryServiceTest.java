package umc.fitme.domain.post.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import umc.fitme.domain.interest.repository.PostInterestRepository;
import umc.fitme.domain.interest.repository.UserInterestRepository;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.post.repository.ViewHistoryRepository;
import umc.fitme.domain.post.enums.ContestCategory;
import umc.fitme.domain.post.util.ContestCategoryResolver;
import umc.fitme.domain.post.util.UniversityTypeResolver;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.domain.user.repository.UserSaveRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private ViewHistoryRepository viewHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetailRepository userDetailRepository;
    @Mock
    private UserInterestRepository userInterestRepository;
    @Mock
    private PostInterestRepository postInterestRepository;
    @Mock
    private UserSaveRepository userSaveRepository;
    @Mock
    private PostSummaryService postSummaryService;

    /**
     * 대학 구분 판정은 실제 university.csv를 읽어야 의미가 있으므로 mock이 아닌 진짜 객체를 넣는다.
     * mock이면 항상 null을 돌려줘 '판정 불가 → 통과' 경로만 타고 매칭 로직이 검증되지 않는다.
     */
    @Spy
    private UniversityTypeResolver universityTypeResolver = new UniversityTypeResolver();

    /** 관심 분야 문자열 매핑을 실제로 검증해야 하므로 mock이 아닌 진짜 객체를 넣는다. */
    @Spy
    private ContestCategoryResolver contestCategoryResolver = new ContestCategoryResolver();

    @InjectMocks
    private PostQueryService postQueryService;

    @Test
    void 마감일이_같으면_조건_일치_수가_많은_공고가_앞에_온다() {
        // 마감일이 1순위이고, 같은 날짜끼리는 조건 일치 수로 순서를 정한다.
        LocalDate sameDeadline = LocalDate.now().plusDays(5);
        Scholarship highlyMatched = scholarship(
                1L,
                sameDeadline,
                "3.0 이상",
                "1~4구간",
                "서울"
        );
        Scholarship unrestricted = scholarship(
                2L,
                sameDeadline,
                "제한 없음",
                "제한 없음",
                "전국"
        );

        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(unrestricted, highlyMatched));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(
                3.5,
                3,
                "서울",
                "가천대학교",
                Set.of("IT/소프트웨어")
        );

        List<PostResponseDTO.PostPreviewDTO> result =
                postQueryService.getClosingSoonPosts(profile, null, 10);

        assertThat(result)
                .extracting(PostResponseDTO.PostPreviewDTO::getPostId)
                .containsExactly(1L, 2L);
    }

    @Test
    void fallsBackToPopularOpenPostsWhenNothingMatches() {
        Scholarship mismatched = scholarship(
                1L,
                LocalDate.now().plusDays(1),
                "4.0 이상",
                "9~10구간",
                "부산"
        );
        Scholarship popular = scholarship(
                9L,
                LocalDate.now().plusDays(3),
                "제한 없음",
                "제한 없음",
                "전국"
        );

        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(mismatched));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());
        when(postRepository.findPopularPostsBySavedCount(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(popular));

        UserFitProfile profile = new UserFitProfile(
                3.5,
                3,
                "서울",
                "한국대학교",
                Set.of("IT/소프트웨어")
        );

        List<PostResponseDTO.PostPreviewDTO> result =
                postQueryService.getClosingSoonPosts(profile, null, 10);

        assertThat(result)
                .extracting(PostResponseDTO.PostPreviewDTO::getPostId)
                .containsExactly(9L);
    }

    @Test
    void reflectsSavedStateWithOneBatchQuery() {
        Scholarship savedPost = scholarship(
                1L,
                LocalDate.now().plusDays(2),
                "3.0 이상",
                "1~4구간",
                "서울"
        );
        UserDetail userDetail = UserDetail.builder()
                .gpa(3.5f)
                .incomeBracket(3)
                .region("서울")
                .universityName("한국대학교")
                .build();

        when(userDetailRepository.findByUserId(7L)).thenReturn(Optional.of(userDetail));
        when(userInterestRepository.findInterestNamesByUserId(7L)).thenReturn(List.of());
        when(userSaveRepository.findSavedPostIdsByUserId(7L)).thenReturn(Set.of(1L));
        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(savedPost));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        List<PostResponseDTO.PostPreviewDTO> result =
                postQueryService.getClosingSoonPosts(7L, null, 10);

        assertThat(result).singleElement()
                .extracting(PostResponseDTO.PostPreviewDTO::getSaved)
                .isEqualTo(true);
    }

    @Test
    void 사용자_대학_구분과_다른_공고는_제외한다() {
        // 4년제 재학생에게 '해외대학'만 대상인 공고는 내려가면 안 된다.
        Scholarship overseasOnly = scholarship(
                1L,
                LocalDate.now().plusDays(2),
                "3.0 이상",
                "1~4구간",
                "서울",
                "해외대학"
        );

        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(overseasOnly));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());
        when(postRepository.findPopularPostsBySavedCount(isNull(), any(PageRequest.class)))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(
                3.5, 3, "서울", "가천대학교", Set.of("IT/소프트웨어")
        );

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10)).isEmpty();
    }

    @Test
    void 사용자_대학_구분이_포함된_공고는_통과시킨다() {
        // 실제 적재 데이터에 있는 형태. 가천대학교는 4년제라 통과해야 한다.
        Scholarship fourYearAndCollege = scholarship(
                1L,
                LocalDate.now().plusDays(2),
                "3.0 이상",
                "1~4구간",
                "서울",
                "4년제(5~6년제포함)전문대(2~3년제)"
        );

        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(fourYearAndCollege));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(
                3.5, 3, "서울", "가천대학교", Set.of("IT/소프트웨어")
        );

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10))
                .singleElement()
                .extracting(PostResponseDTO.PostPreviewDTO::getPostId)
                .isEqualTo(1L);
    }

    @Test
    void 문장으로_적힌_대학_조건은_판정하지_않고_통과시킨다() {
        // '도내 대학 해외교환 장학생'처럼 구분 키워드가 없는 조건은 기계적으로 판정할 수 없다.
        // 못 알아본 것 때문에 지원 가능한 공고를 가리지 않는다.
        Scholarship sentenceRequirement = scholarship(
                1L,
                LocalDate.now().plusDays(2),
                "3.0 이상",
                "1~4구간",
                "서울",
                "도내 대학 해외교환 장학생"
        );

        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(sentenceRequirement));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(
                3.5, 3, "서울", "가천대학교", Set.of("IT/소프트웨어")
        );

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10)).hasSize(1);
    }

    @Test
    void 마감_임박_공고는_마감일이_가까운_순서로_내려간다() {
        // 조건 일치 수가 적어도 마감이 급하면 앞에 와야 한다.
        // 2L은 대학 조건이 있어 일치 수가 더 크지만 마감일이 늦다.
        Scholarship urgent = scholarship(
                1L,
                LocalDate.now().plusDays(1),
                "제한 없음",
                "제한 없음",
                "제한 없음",
                "제한 없음"
        );
        Scholarship laterButBetterMatch = scholarship(
                2L,
                LocalDate.now().plusDays(9),
                "3.0 이상",
                "1~4구간",
                "서울",
                "4년제(5~6년제포함)"
        );

        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(laterButBetterMatch, urgent));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(
                3.5, 3, "서울", "가천대학교", Set.of("IT/소프트웨어")
        );

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10))
                .extracting(PostResponseDTO.PostPreviewDTO::getPostId)
                .containsExactly(1L, 2L);
    }

    @Test
    void 인기_공고는_공모전과_장학금을_3대2로_섞어_내려준다() {
        when(postRepository.findRandomPostsByType(eq(PostType.CONTEST), any(PageRequest.class)))
                .thenReturn(contests(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
        when(postRepository.findRandomPostsByType(eq(PostType.SCHOLARSHIP), any(PageRequest.class)))
                .thenReturn(scholarships(11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L));

        PostResponseDTO.PopularPostListDTO result = postQueryService.getPopularPosts(null, null, 8);

        assertThat(result.getPopularPosts()).hasSize(8);
        assertThat(countByType(result, PostType.CONTEST)).isEqualTo(5);
        assertThat(countByType(result, PostType.SCHOLARSHIP)).isEqualTo(3);
    }

    @Test
    void 공모전이_배정량보다_적으면_장학금으로_채워_요청한_개수를_유지한다() {
        when(postRepository.findRandomPostsByType(eq(PostType.CONTEST), any(PageRequest.class)))
                .thenReturn(contests(1L, 2L));
        when(postRepository.findRandomPostsByType(eq(PostType.SCHOLARSHIP), any(PageRequest.class)))
                .thenReturn(scholarships(11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L));

        PostResponseDTO.PopularPostListDTO result = postQueryService.getPopularPosts(null, null, 8);

        assertThat(result.getPopularPosts()).hasSize(8);
        assertThat(countByType(result, PostType.CONTEST)).isEqualTo(2);
        assertThat(countByType(result, PostType.SCHOLARSHIP)).isEqualTo(6);
    }

    @Test
    void 장학금이_배정량보다_적으면_공모전으로_채운다() {
        when(postRepository.findRandomPostsByType(eq(PostType.CONTEST), any(PageRequest.class)))
                .thenReturn(contests(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
        when(postRepository.findRandomPostsByType(eq(PostType.SCHOLARSHIP), any(PageRequest.class)))
                .thenReturn(scholarships(11L));

        PostResponseDTO.PopularPostListDTO result = postQueryService.getPopularPosts(null, null, 8);

        assertThat(result.getPopularPosts()).hasSize(8);
        assertThat(countByType(result, PostType.CONTEST)).isEqualTo(7);
        assertThat(countByType(result, PostType.SCHOLARSHIP)).isEqualTo(1);
    }

    @Test
    void 공고가_모자라면_있는_만큼만_내려준다() {
        when(postRepository.findRandomPostsByType(eq(PostType.CONTEST), any(PageRequest.class)))
                .thenReturn(contests(1L));
        when(postRepository.findRandomPostsByType(eq(PostType.SCHOLARSHIP), any(PageRequest.class)))
                .thenReturn(scholarships(11L));

        PostResponseDTO.PopularPostListDTO result = postQueryService.getPopularPosts(null, null, 8);

        assertThat(result.getPopularPosts()).hasSize(2);
    }

    @Test
    void 인기_공고에_사용자가_찜한_공고는_saved가_true로_내려간다() {
        when(postRepository.findRandomPostsByType(eq(PostType.CONTEST), any(PageRequest.class)))
                .thenReturn(contests(1L, 2L, 3L, 4L, 5L));
        when(postRepository.findRandomPostsByType(eq(PostType.SCHOLARSHIP), any(PageRequest.class)))
                .thenReturn(scholarships(11L, 12L, 13L));
        when(userSaveRepository.findSavedPostIdsByUserId(7L)).thenReturn(Set.of(2L, 12L));

        PostResponseDTO.PopularPostListDTO result = postQueryService.getPopularPosts(7L, null, 8);

        assertThat(result.getPopularPosts())
                .filteredOn(PostResponseDTO.PopularPostDTO::getSaved)
                .extracting(PostResponseDTO.PopularPostDTO::getPostId)
                .containsExactlyInAnyOrder(2L, 12L);
    }

    @Test
    void 비로그인이면_찜_조회_없이_모두_false로_내려간다() {
        when(postRepository.findRandomPostsByType(eq(PostType.CONTEST), any(PageRequest.class)))
                .thenReturn(contests(1L, 2L, 3L, 4L, 5L));
        when(postRepository.findRandomPostsByType(eq(PostType.SCHOLARSHIP), any(PageRequest.class)))
                .thenReturn(scholarships(11L, 12L, 13L));

        PostResponseDTO.PopularPostListDTO result = postQueryService.getPopularPosts(null, null, 8);

        assertThat(result.getPopularPosts())
                .extracting(PostResponseDTO.PopularPostDTO::getSaved)
                .containsOnly(false);
        verify(userSaveRepository, never()).findSavedPostIdsByUserId(any());
    }

    @Test
    void 공모전_상세는_contestDetail을_채우고_scholarshipDetail은_비운다() {
        Contest contest = Contest.builder()
                .id(50L)
                .postType(PostType.CONTEST)
                .title("테스트 공모전")
                .organizer("테스트 주최")
                .applyStartAt(LocalDate.now())
                .applyEndAt(LocalDate.now().plusDays(10))
                .applicationMethod("홈페이지 지원")
                .applicationUrl("https://example.com")
                .posterImageUrl("https://example.com/poster.jpg")
                .target("전국 대학생")
                .participantLimit("개인 또는 팀(4인 이하)")
                .rewardTotal("상금 500만원")
                .build();

        when(postRepository.findById(50L)).thenReturn(Optional.of(contest));
        // 최근 조회 이력이 없으면 새로 만들면서 사용자를 조회한다.
        when(viewHistoryRepository.findByUserIdAndPostId(7L, 50L)).thenReturn(Optional.empty());
        when(userRepository.findById(7L)).thenReturn(Optional.of(User.builder().id(7L).build()));
        when(userSaveRepository.findSavedPostIdsByUserId(7L)).thenReturn(Set.of(50L));

        PostResponseDTO.PostDetailDTO result = postQueryService.getContestPostDetail(7L, 50L);

        assertThat(result.getType()).isEqualTo("CONTEST");
        assertThat(result.getSaved()).isTrue();
        assertThat(result.getScholarshipDetail()).isNull();
        assertThat(result.getContestDetail()).isNotNull();
        assertThat(result.getContestDetail().getPosterImageUrl()).isEqualTo("https://example.com/poster.jpg");
        assertThat(result.getContestDetail().getTarget()).isEqualTo("전국 대학생");
        assertThat(result.getContestDetail().getParticipantLimit()).isEqualTo("개인 또는 팀(4인 이하)");
        assertThat(result.getContestDetail().getRewardTotal()).isEqualTo("상금 500만원");
    }

    @Test
    void 공모전_상세_경로에_장학금_id가_오면_찾을_수_없다() {
        Scholarship scholarship = scholarship(
                60L, LocalDate.now().plusDays(5), "제한 없음", "제한 없음", "제한 없음");
        when(postRepository.findById(60L)).thenReturn(Optional.of(scholarship));

        assertThatThrownBy(() -> postQueryService.getContestPostDetail(7L, 60L))
                .isInstanceOf(ProjectException.class);
    }

    private long countByType(PostResponseDTO.PopularPostListDTO result, PostType type) {
        return result.getPopularPosts().stream()
                .filter(post -> type.name().equals(post.getType()))
                .count();
    }

    @Test
    void 공모전은_사용자가_고른_분야만_내려간다() {
        // 사용자는 디자인만 골랐다. IT 공모전은 빠져야 한다.
        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(contest(1L, ContestCategory.DESIGN), contest(2L, ContestCategory.IT)));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(3.5, 3, "서울", "가천대학교", Set.of("디자인"));

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10))
                .extracting(PostResponseDTO.PostPreviewDTO::getPostId)
                .containsExactly(1L);
    }

    @Test
    void 여러_분야를_고르면_그중_하나만_맞아도_내려간다() {
        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(
                        contest(1L, ContestCategory.DESIGN),
                        contest(2L, ContestCategory.IT),
                        contest(3L, ContestCategory.MARKETING)));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile =
                new UserFitProfile(3.5, 3, "서울", "가천대학교", Set.of("디자인", "IT/개발"));

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10))
                .extracting(PostResponseDTO.PostPreviewDTO::getPostId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void 기타를_고른_사용자에게는_모든_카테고리가_내려간다() {
        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(contest(1L, ContestCategory.DESIGN), contest(2L, ContestCategory.IT)));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(3.5, 3, "서울", "가천대학교", Set.of("기타"));

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10)).hasSize(2);
    }

    @Test
    void 카테고리가_기타인_공모전은_무엇을_골랐든_내려간다() {
        // 분류가 애매해 기타로 넣은 공고를 가리면 사용자가 존재조차 알 수 없다.
        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(contest(1L, ContestCategory.ETC), contest(2L, ContestCategory.IT)));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(3.5, 3, "서울", "가천대학교", Set.of("디자인"));

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10))
                .extracting(PostResponseDTO.PostPreviewDTO::getPostId)
                .containsExactly(1L);
    }

    @Test
    void 카테고리가_없는_공모전은_판정하지_않고_내려간다() {
        // 공용 DB의 contest_category가 아직 비어 있어 이 경로가 실제로 쓰인다.
        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(contest(1L, null)));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(3.5, 3, "서울", "가천대학교", Set.of("디자인"));

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10)).hasSize(1);
    }

    @Test
    void 알아볼_수_없는_관심분야만_고른_사용자는_거르지_않는다() {
        // 직접 입력한 '로봇공학'은 카테고리로 바꿀 수 없다. 못 알아본 것으로 공고를 가리지 않는다.
        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(contest(1L, ContestCategory.DESIGN), contest(2L, ContestCategory.IT)));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(3.5, 3, "서울", "가천대학교", Set.of("로봇공학"));

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10)).hasSize(2);
    }

    private Contest contest(Long id, ContestCategory category) {
        return Contest.builder()
                .id(id)
                .postType(PostType.CONTEST)
                .contestCategory(category)
                .title("테스트 공모전 " + id)
                .organizer("테스트 주최")
                .applyStartAt(LocalDate.now())
                .applyEndAt(LocalDate.now().plusDays(3))
                .applicationMethod("홈페이지 지원")
                .applicationUrl("https://example.com")
                .build();
    }

    private List<Post> contests(Long... ids) {
        return java.util.Arrays.stream(ids)
                .<Post>map(id -> Contest.builder()
                        .id(id)
                        .postType(PostType.CONTEST)
                        .title("테스트 공모전 " + id)
                        .organizer("테스트 주최")
                        .applyStartAt(LocalDate.now())
                        .applyEndAt(LocalDate.now().plusDays(10))
                        .applicationMethod("홈페이지 지원")
                        .applicationUrl("https://example.com")
                        .build())
                .toList();
    }

    private List<Post> scholarships(Long... ids) {
        return java.util.Arrays.stream(ids)
                .<Post>map(id -> scholarship(
                        id, LocalDate.now().plusDays(10), "제한 없음", "제한 없음", "제한 없음"))
                .toList();
    }

    private Scholarship scholarship(
            Long id,
            LocalDate deadline,
            String gradeRequirement,
            String incomeRequirement,
            String regionRequirement
    ) {
        return scholarship(
                id, deadline, gradeRequirement, incomeRequirement, regionRequirement, "제한 없음"
        );
    }

    private Scholarship scholarship(
            Long id,
            LocalDate deadline,
            String gradeRequirement,
            String incomeRequirement,
            String regionRequirement,
            String universityRequirement
    ) {
        return Scholarship.builder()
                .id(id)
                .postType(PostType.SCHOLARSHIP)
                .title("테스트 장학금 " + id)
                .organizer("테스트 기관")
                .applyStartAt(LocalDate.now())
                .applyEndAt(deadline)
                .applicationMethod("홈페이지 지원")
                .applicationUrl("https://example.com")
                .gradeRequirement(gradeRequirement)
                .incomeRequirement(incomeRequirement)
                .regionRequirement(regionRequirement)
                .universityRequirement(universityRequirement)
                .supportAmount("100만원")
                .build();
    }
}
