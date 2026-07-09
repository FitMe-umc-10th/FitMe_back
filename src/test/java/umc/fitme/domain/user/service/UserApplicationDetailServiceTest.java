package umc.fitme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.user.dto.UserApplicationResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.enums.Status;
import umc.fitme.domain.user.repository.UserApplicationRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApplicationDetailServiceTest {

    @Mock
    private UserApplicationRepository userApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private UserApplicationService userApplicationService;

    @Test
    @DisplayName("지원 이력 상세 조회 시 지원 상태, 메모, 공고 정보를 반환한다")
    void getDetail_returnsApplicationAndPostDetail() {
        // given
        User user = createUser();
        Post post = createPost();

        UserApplication userApplication = UserApplication.builder()
                .user(user)
                .post(post)
                .status(Status.FINAL_PASSED)
                .isApplied(true)
                .memo("서류 제출 완료")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(userApplication));

        // when
        UserApplicationResponseDto.DetailResponse response =
                userApplicationService.getDetail(1L);

        // then
        assertThat(response.status()).isEqualTo(Status.FINAL_PASSED.name());
        assertThat(response.isApplied()).isTrue();
        assertThat(response.memo()).isEqualTo("서류 제출 완료");

        assertThat(response.post()).isNotNull();
        assertThat(response.post().postId()).isEqualTo(1L);
        assertThat(response.post().postType()).isEqualTo(PostType.CONTEST.name());
        assertThat(response.post().title()).isEqualTo("테스트 공모전");
        assertThat(response.post().organizer()).isEqualTo("테스트 기관");
    }

    @Test
    @DisplayName("지원 이력 상세 조회 시 공고 조회수가 증가한다")
    void getDetail_increasesPostViewCount() {
        // given
        User user = createUser();
        Post post = createPost();

        UserApplication userApplication = UserApplication.builder()
                .user(user)
                .post(post)
                .status(Status.PENDING_RESULT)
                .isApplied(true)
                .memo(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(userApplication));

        // when
        UserApplicationResponseDto.DetailResponse response =
                userApplicationService.getDetail(1L);

        // then
        assertThat(response.post().viewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("본인 소유가 아닌 지원 이력 상세 조회 시 예외를 던진다")
    void getDetail_otherUserApplication_throwsException() {
        // given
        User user = createUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUser(999L, user))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userApplicationService.getDetail(999L))
                .isInstanceOf(ProjectException.class)
                .extracting("baseErrorCode")
                .isEqualTo(GeneralErrorCode.USER_APPLICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("마감된 공고의 지원 이력도 상세 조회할 수 있다")
    void getDetail_closedPost_returnsDetail() {
        // given
        User user = createUser();

        Post closedPost = Post.builder()
                .id(1L)
                .postType(PostType.CONTEST)
                .title("마감된 공모전")
                .organizer("테스트 기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 7, 30))
                .summary("마감된 공고입니다.")
                .applicationMethod("공식 홈페이지 접수")
                .applicationUrl("https://example.com/apply")
                .build();

        UserApplication userApplication = UserApplication.builder()
                .user(user)
                .post(closedPost)
                .status(Status.PENDING_RESULT)
                .isApplied(true)
                .memo("마감 공고 메모")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(userApplication));

        // when
        UserApplicationResponseDto.DetailResponse response =
                userApplicationService.getDetail(1L);

        // then
        assertThat(response.post().title()).isEqualTo("마감된 공모전");
        assertThat(response.post().applyEndAt()).isEqualTo(LocalDate.of(2026, 7, 30));
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .build();
    }

    private Post createPost() {
        return Post.builder()
                .id(1L)
                .postType(PostType.CONTEST)
                .title("테스트 공모전")
                .organizer("테스트 기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 7, 31))
                .summary("테스트용 공고입니다.")
                .applicationMethod("공식 홈페이지 접수")
                .applicationUrl("https://example.com/apply")
                .build();
    }
}