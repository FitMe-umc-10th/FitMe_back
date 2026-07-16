package umc.fitme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.user.dto.UserApplicationRequestDto;
import umc.fitme.domain.user.dto.UserApplicationResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.enums.Status;
import umc.fitme.domain.user.exception.code.UserApplicationErrorCode;
import umc.fitme.domain.user.repository.UserApplicationRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationDeleteServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long USER_APPLICATION_ID = 1L;
    private static final Long POST_ID = 1L;

    @Mock
    private UserApplicationRepository userApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private UserApplicationService userApplicationService;

    @Test
    @DisplayName("지원 이력을 삭제하면 deletedAt이 저장된다")
    void delete_success_setsDeletedAt() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .build();

        Post post = mock(Post.class);

        UserApplication userApplication = UserApplication.builder()
                .id(USER_APPLICATION_ID)
                .user(user)
                .post(post)
                .status(Status.PENDING_RESULT)
                .isApplied(true)
                .build();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(
                USER_APPLICATION_ID,
                user
        )).thenReturn(Optional.of(userApplication));

        // when
        UserApplicationResponseDto.DeleteResponse response =
                userApplicationService.delete(USER_ID, USER_APPLICATION_ID);

        // then
        assertThat(userApplication.getDeletedAt()).isNotNull();
        assertThat(response.userApplicationId()).isEqualTo(USER_APPLICATION_ID);
    }

    @Test
    @DisplayName("삭제 대상 지원 이력이 없으면 예외를 던진다")
    void delete_notFound_throwsException() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .build();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(
                USER_APPLICATION_ID,
                user
        )).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                userApplicationService.delete(USER_ID, USER_APPLICATION_ID)
        )
                .isInstanceOf(ProjectException.class)
                .satisfies(exception -> {
                    ProjectException projectException = (ProjectException) exception;
                    assertThat(projectException.getErrorCode())
                            .isEqualTo(UserApplicationErrorCode.USER_APPLICATION_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("삭제된 동일 공고에 재지원하면 새 지원 이력을 저장한다")
    void create_afterDelete_savesNewApplication() {
        // given
        User user = User.builder()
                .id(USER_ID)
                .build();

        Post post = mock(Post.class);

        UserApplicationRequestDto.CreateRequest request =
                new UserApplicationRequestDto.CreateRequest(POST_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(postRepository.findById(POST_ID))
                .thenReturn(Optional.of(post));
        when(userApplicationRepository.findByUserAndPostAndDeletedAtIsNull(user, post))
                .thenReturn(Optional.empty());
        when(userApplicationRepository.save(any(UserApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userApplicationService.create(USER_ID, request);

        // then
        verify(userApplicationRepository).save(any(UserApplication.class));
    }
}