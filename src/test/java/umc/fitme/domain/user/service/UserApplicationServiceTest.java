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
import umc.fitme.domain.user.dto.UserApplicationRequestDto;
import umc.fitme.domain.user.dto.UserApplicationResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.entity.mapping.UserApplicationPostSnapshot;
import umc.fitme.domain.user.enums.Status;
import umc.fitme.domain.user.repository.UserApplicationRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.domain.user.repository.UserApplicationPostSnapshotRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;
import umc.fitme.domain.user.exception.code.UserApplicationErrorCode;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long POST_ID = 1L;
    private static final Long USER_APPLICATION_ID = 1L;

    @Mock
    private UserApplicationRepository userApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserApplicationPostSnapshotRepository userApplicationPostSnapshotRepository;

    @InjectMocks
    private UserApplicationService userApplicationService;

    @Test
    @DisplayName("지원 이력 자동 등록 시 기존 이력이 있으면 새로 저장하지 않고 기존 이력을 반환한다.")
    void create_existingUserApplication_returnsExistingApplication() {
        // given
        User user = createUser();
        Post post = createPost();

        UserApplication existingApplication = UserApplication.builder()
                .user(user)
                .post(post)
                .status(Status.NONE)
                .isApplied(false)
                .memo(null)
                .build();

        UserApplicationRequestDto.CreateRequest request =
                new UserApplicationRequestDto.CreateRequest(POST_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(userApplicationRepository.findByUserAndPostAndDeletedAtIsNull(user, post))
                .thenReturn(Optional.of(existingApplication));

        // when
        UserApplicationResponseDto.CreateResponse response =
                userApplicationService.create(USER_ID, request);

        // then
        assertThat(response.postId()).isEqualTo(POST_ID);
        assertThat(response.status()).isEqualTo(Status.NONE.name());
        assertThat(response.isApplied()).isFalse();
        assertThat(response.applicationUrl()).isEqualTo("https://example.com/apply");

        verify(userApplicationRepository, never()).save(any(UserApplication.class));
        verify(userApplicationPostSnapshotRepository, never())
                .save(any(UserApplicationPostSnapshot.class));
    }

    @Test
    @DisplayName("지원 이력 목록 조회 시 카드 이미지 URL을 반환한다.")
    void getList_returnsImageUrl() {
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

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findAllByUserAndDeletedAtIsNullAndStatusInOrderByUpdatedAtDescIdDesc(
                eq(user),
                anyList()
        )).thenReturn(List.of(userApplication));

        // when
        UserApplicationResponseDto.ListResponse response =
                userApplicationService.getList(USER_ID, "IN_PROGRESS");

        // then
        assertThat(response.userApplications()).hasSize(1);
        assertThat(response.userApplications().getFirst().imageUrl())
                .isEqualTo(post.getImageUrl());
    }

    @Test
    @DisplayName("유효하지 않은 탭으로 목록 조회 시 예외를 던진다.")
    void getList_invalidTab_throwsException() {
        // given
        User user = createUser();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userApplicationService.getList(USER_ID, "INVALID"))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserApplicationErrorCode.INVALID_USER_APPLICATION_TAB);
    }

    @Test
    @DisplayName("지원 상태를 변경하면 status가 변경되고 isApplied가 true가 된다.")
    void updateStatus_validStatus_updatesStatusAndIsApplied() {
        // given
        User user = createUser();
        Post post = createPost();

        UserApplication userApplication = UserApplication.builder()
                .user(user)
                .post(post)
                .status(Status.NONE)
                .isApplied(false)
                .memo(null)
                .build();

        UserApplicationRequestDto.UpdateStatusRequest request =
                new UserApplicationRequestDto.UpdateStatusRequest(Status.PENDING_RESULT);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(USER_APPLICATION_ID, user))
                .thenReturn(Optional.of(userApplication));

        // when
        UserApplicationResponseDto.UpdateStatusResponse response =
                userApplicationService.updateStatus(USER_ID, USER_APPLICATION_ID, request);

        // then
        assertThat(response.status()).isEqualTo(Status.PENDING_RESULT.name());
        assertThat(response.isApplied()).isTrue();
    }

    @Test
    @DisplayName("지원 상태를 NONE으로 변경하려고 하면 예외를 던진다.")
    void updateStatus_noneStatus_throwsException() {
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

        UserApplicationRequestDto.UpdateStatusRequest request =
                new UserApplicationRequestDto.UpdateStatusRequest(Status.NONE);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(USER_APPLICATION_ID, user))
                .thenReturn(Optional.of(userApplication));

        // when & then
        assertThatThrownBy(() -> userApplicationService.updateStatus(USER_ID, USER_APPLICATION_ID, request))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserApplicationErrorCode.INVALID_USER_APPLICATION_STATUS);
    }

    @Test
    @DisplayName("삭제된 지원 이력은 상태를 변경할 수 없다.")
    void updateStatus_deletedApplication_throwsException() {
        // given
        User user = createUser();

        UserApplicationRequestDto.UpdateStatusRequest request = new UserApplicationRequestDto.UpdateStatusRequest(Status.PENDING_RESULT);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(USER_APPLICATION_ID, user))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userApplicationService.updateStatus(USER_ID, USER_APPLICATION_ID, request))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserApplicationErrorCode.USER_APPLICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제된 지원 이력은 메모를 수정할 수 없다.")
    void updateMemo_deletedApplication_throwsException() {
        // given
        User user = createUser();

        UserApplicationRequestDto.UpdateMemoRequest request = new UserApplicationRequestDto.UpdateMemoRequest("수정할 메모");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(USER_APPLICATION_ID, user))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userApplicationService.updateMemo(USER_ID, USER_APPLICATION_ID, request))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserApplicationErrorCode.USER_APPLICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("메모 수정 시 앞뒤 공백을 제거하여 저장한다.")
    void updateMemo_trimMemo_savesTrimmedMemo() {
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

        UserApplicationRequestDto.UpdateMemoRequest request =
                new UserApplicationRequestDto.UpdateMemoRequest("  서류 제출 완료  ");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(USER_APPLICATION_ID, user))
                .thenReturn(Optional.of(userApplication));

        // when
        UserApplicationResponseDto.UpdateMemoResponse response =
                userApplicationService.updateMemo(USER_ID, USER_APPLICATION_ID, request);

        // then
        assertThat(response.memo()).isEqualTo("서류 제출 완료");
    }

    @Test
    @DisplayName("공백만 입력한 메모는 null로 저장한다.")
    void updateMemo_blankMemo_savesNull() {
        // given
        User user = createUser();
        Post post = createPost();

        UserApplication userApplication = UserApplication.builder()
                .user(user)
                .post(post)
                .status(Status.PENDING_RESULT)
                .isApplied(true)
                .memo("기존 메모")
                .build();

        UserApplicationRequestDto.UpdateMemoRequest request =
                new UserApplicationRequestDto.UpdateMemoRequest("     ");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(USER_APPLICATION_ID, user))
                .thenReturn(Optional.of(userApplication));

        // when
        UserApplicationResponseDto.UpdateMemoResponse response =
                userApplicationService.updateMemo(USER_ID, USER_APPLICATION_ID, request);

        // then
        assertThat(response.memo()).isNull();
    }

    @Test
    @DisplayName("원본 메모가 1000자를 초과하면 예외를 던진다.")
    void updateMemo_over1000Characters_throwsException() {
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

        String over1000Memo = "a".repeat(1001);

        UserApplicationRequestDto.UpdateMemoRequest request =
                new UserApplicationRequestDto.UpdateMemoRequest(over1000Memo);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(USER_APPLICATION_ID, user))
                .thenReturn(Optional.of(userApplication));

        // when & then
        assertThatThrownBy(() -> userApplicationService.updateMemo(USER_ID, USER_APPLICATION_ID, request))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserApplicationErrorCode.MEMO_TOO_LONG);
    }

    @Test
    @DisplayName("존재하지 않는 지원 이력 메모 수정 시 예외를 던진다.")
    void updateMemo_notFoundApplication_throwsException() {
        // given
        User user = createUser();
        Long notFoundUserApplicationId = 999L;

        UserApplicationRequestDto.UpdateMemoRequest request =
                new UserApplicationRequestDto.UpdateMemoRequest("메모");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userApplicationRepository.findByIdAndUserAndDeletedAtIsNull(notFoundUserApplicationId, user))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userApplicationService.updateMemo(USER_ID, notFoundUserApplicationId, request))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserApplicationErrorCode.USER_APPLICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("새 지원 이력이 생성되면 공고 스냅샷도 함꼐 저장한다.")
    void create_newUserApplication_savesSnapshot() {
        // given
        User user = createUser();
        Post post = createPost();

        UserApplicationRequestDto.CreateRequest request =
                new UserApplicationRequestDto.CreateRequest(POST_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(userApplicationRepository.findByUserAndPostAndDeletedAtIsNull(user, post))
                .thenReturn(Optional.empty());
        when(userApplicationRepository.save(any(UserApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userApplicationService.create(USER_ID, request);

        // then
        verify(userApplicationRepository).save(any(UserApplication.class));
        verify(userApplicationPostSnapshotRepository)
                .save(any(UserApplicationPostSnapshot.class));
    }

    private User createUser() {
        return User.builder()
                .id(USER_ID)
                .build();
    }

    private Post createPost() {
        return Post.builder()
                .id(POST_ID)
                .postType(PostType.CONTEST)
                .title("테스트 공모전")
                .organizer("테스트 기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 7, 31))
                .applicationMethod("공식 홈페이지 접수")
                .applicationUrl("https://example.com/apply")
                .imageUrl("https://example.com/image.png")
                .build();
    }
}