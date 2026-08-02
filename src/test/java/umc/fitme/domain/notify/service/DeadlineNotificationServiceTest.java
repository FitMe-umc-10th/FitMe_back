package umc.fitme.domain.notify.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.notify.dto.DeadlineEmailReminderTarget;
import umc.fitme.domain.notify.entity.Notification;
import umc.fitme.domain.notify.enums.NotificationType;
import umc.fitme.domain.notify.repository.NotificationRepository;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.repository.UserSaveRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeadlineNotificationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 31);

    @Mock
    private UserSaveRepository userSaveRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private DeadlineNotificationService deadlineNotificationService;

    private User createUser(Long id) {
        return User.builder().id(id).build();
    }

    private Post createPost(Long id, LocalDate applyEndAt) {
        return Post.builder()
                .id(id)
                .postType(PostType.SCHOLARSHIP)
                .title("국가장학금 1유형")
                .organizer("한국장학재단")
                .applyStartAt(TODAY.minusDays(10))
                .applyEndAt(applyEndAt)
                .build();
    }

    private DeadlineEmailReminderTarget createTarget(Long userId, Long postId, LocalDate applyEndAt) {
        return new DeadlineEmailReminderTarget(
                createUser(userId),
                createPost(postId, applyEndAt),
                "notify@example.com"
        );
    }

    @Test
    @DisplayName("오늘 기준 D-7, D-3, D-1 마감일을 알림 대상으로 조회한다")
    void createDeadlineNotifications_findsTargetsByReminderDates() {
        given(userSaveRepository.findDeadlineEmailReminderTargets(anyList())).willReturn(List.of());

        deadlineNotificationService.createDeadlineNotifications(TODAY);

        ArgumentCaptor<List<LocalDate>> captor = ArgumentCaptor.forClass(List.class);
        verify(userSaveRepository).findDeadlineEmailReminderTargets(captor.capture());
        assertThat(captor.getValue()).containsExactly(
                LocalDate.of(2026, 8, 7),
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 1)
        );
    }

    @Test
    @DisplayName("마감 임박 대상이면 인앱 알림을 생성한다")
    void createDeadlineNotifications_savesNotification() {
        given(userSaveRepository.findDeadlineEmailReminderTargets(anyList()))
                .willReturn(List.of(createTarget(1L, 100L, TODAY.plusDays(7))));
        given(notificationRepository.existsSince(anyLong(), anyLong(), any(), any())).willReturn(false);

        deadlineNotificationService.createDeadlineNotifications(TODAY);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUser().getId()).isEqualTo(1L);
        assertThat(saved.getPost().getId()).isEqualTo(100L);
        assertThat(saved.getNotificationType()).isEqualTo(NotificationType.LAST_MINUTE);
        assertThat(saved.getTitle()).isEqualTo("국가장학금 1유형");
        assertThat(saved.getMessage()).isEqualTo("마감일이 7일 남았습니다. 잊지 말고 지원하세요!");
        assertThat(saved.getIsRead()).isFalse();
    }

    @Test
    @DisplayName("같은 날 이미 만들어진 알림이 있으면 중복 생성하지 않는다")
    void createDeadlineNotifications_skipsWhenAlreadyCreated() {
        given(userSaveRepository.findDeadlineEmailReminderTargets(anyList()))
                .willReturn(List.of(createTarget(1L, 100L, TODAY.plusDays(3))));
        given(notificationRepository.existsSince(
                eq(1L),
                eq(100L),
                eq(NotificationType.LAST_MINUTE),
                eq(TODAY.atStartOfDay())
        )).willReturn(true);

        deadlineNotificationService.createDeadlineNotifications(TODAY);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("D-7, D-3, D-1에 해당하지 않는 마감일은 알림을 만들지 않는다")
    void createDeadlineNotifications_skipsWhenNotReminderDay() {
        given(userSaveRepository.findDeadlineEmailReminderTargets(anyList()))
                .willReturn(List.of(createTarget(1L, 100L, TODAY.plusDays(5))));

        deadlineNotificationService.createDeadlineNotifications(TODAY);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("한 건이 실패해도 나머지 대상의 알림 생성은 계속 진행한다")
    void createDeadlineNotifications_continuesAfterFailure() {
        given(userSaveRepository.findDeadlineEmailReminderTargets(anyList()))
                .willReturn(List.of(
                        createTarget(1L, 100L, TODAY.plusDays(7)),
                        createTarget(2L, 200L, TODAY.plusDays(1))
                ));
        given(notificationRepository.existsSince(anyLong(), anyLong(), any(), any())).willReturn(false);
        given(notificationRepository.save(any()))
                .willThrow(new RuntimeException("첫 번째 저장 실패"))
                .willReturn(null);

        deadlineNotificationService.createDeadlineNotifications(TODAY);

        verify(notificationRepository, times(2)).save(any());
    }
}
