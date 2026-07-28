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
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.post.repository.ViewHistoryRepository;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.domain.user.repository.UserSaveRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.isNull;
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
