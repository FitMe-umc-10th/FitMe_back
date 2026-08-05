package umc.fitme.domain.post.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.mockito.ArgumentMatchers.*;
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

    @InjectMocks
    private PostQueryService postQueryService;

    @Test
    void matchingCountTakesPriorityThenDeadline() {
        Scholarship highlyMatched = scholarship(
                1L,
                LocalDate.now().plusDays(5),
                "3.0 이상",
                "1~4구간",
                "서울"
        );
        Scholarship unrestricted = scholarship(
                2L,
                LocalDate.now().plusDays(1),
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
                "한국대학교",
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
    void excludesScholarshipWhenUniversityRequirementDoesNotMatch() {
        Scholarship differentUniversityOnly = scholarship(
                1L,
                LocalDate.now().plusDays(2),
                "3.0 이상",
                "1~4구간",
                "서울",
                "다른대학교"
        );

        when(postRepository.findClosingSoonPosts(isNull(), any(PageRequest.class)))
                .thenReturn(List.of(differentUniversityOnly));
        when(postInterestRepository.findPostInterestNamesByPostIds(anyCollection()))
                .thenReturn(List.of());
        when(postRepository.findPopularPostsBySavedCount(isNull(), any(PageRequest.class)))
                .thenReturn(List.of());

        UserFitProfile profile = new UserFitProfile(
                3.5, 3, "서울", "한국대학교", Set.of("IT/소프트웨어")
        );

        assertThat(postQueryService.getClosingSoonPosts(profile, null, 10)).isEmpty();
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
