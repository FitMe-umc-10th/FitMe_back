package umc.fitme.domain.notify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.notify.dto.DeadlineEmailReminderTarget;
import umc.fitme.domain.notify.entity.DeadlineEmailNotificationLog;
import umc.fitme.domain.notify.enums.DeadlineReminderType;
import umc.fitme.domain.notify.enums.EmailSendStatus;
import umc.fitme.domain.notify.repository.DeadlineEmailNotificationLogRepository;
import umc.fitme.domain.notify.service.DeadlineEmailNotificationService;
import umc.fitme.domain.notify.service.DeadlineEmailSender;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.repository.UserSaveRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadlineEmailNotificationServiceTest {

    @Mock
    private UserSaveRepository userSaveRepository;

    @Mock
    private DeadlineEmailNotificationLogRepository deadlineEmailNotificationLogRepository;

    @Mock
    private DeadlineEmailSender deadlineEmailSender;

    @InjectMocks
    private DeadlineEmailNotificationService deadlineEmailNotificationService;

    @Test
    @DisplayName("오늘 기준 D-7, D-3, D-1 마감일을 알림 대상으로 조회한다")
    void sendDeadlineReminderEmails_findTargetsByReminderDates() {
        LocalDate today = LocalDate.of(2026, 7, 18);

        when(userSaveRepository.findDeadlineEmailReminderTargets(anyList()))
                .thenReturn(List.of());

        deadlineEmailNotificationService.sendDeadlineReminderEmails(today);

        ArgumentCaptor<List<LocalDate>> captor = ArgumentCaptor.forClass(List.class);
        verify(userSaveRepository).findDeadlineEmailReminderTargets(captor.capture());
        assertEquals(
                List.of(
                        LocalDate.of(2026, 7, 25),
                        LocalDate.of(2026, 7, 21),
                        LocalDate.of(2026, 7, 19)
                ),
                captor.getValue()
        );
    }

    @Test
    @DisplayName("이미 발송 이력이 있으면 이메일을 보내지 않는다")
    void sendDeadlineReminderEmails_skipAlreadySentTarget() {
        LocalDate today = LocalDate.of(2026, 7, 18);
        User user = User.builder().id(1L).email("user@example.com").build();
        Post post = createPost(1L, today.plusDays(7));

        DeadlineEmailReminderTarget target =
                new DeadlineEmailReminderTarget(user, post, "notify@example.com");

        when(userSaveRepository.findDeadlineEmailReminderTargets(anyList()))
                .thenReturn(List.of(target));

        when(deadlineEmailNotificationLogRepository.existsByUserAndPostAndReminderTypeAndApplyEndAt(
                eq(user),
                eq(post),
                eq(DeadlineReminderType.D_MINUS_7),
                eq(post.getApplyEndAt())
        )).thenReturn(true);

        deadlineEmailNotificationService.sendDeadlineReminderEmails(today);

        verify(deadlineEmailSender, never())
                .send(anyString(), any(Post.class), any(DeadlineReminderType.class));
        verify(deadlineEmailNotificationLogRepository, never()).save(any());
    }

    private Post createPost(Long id, LocalDate applyEndAt) {
        return Post.builder()
                .id(id)
                .postType(PostType.SCHOLARSHIP)
                .title("Test Post")
                .organizer("Test Organizer")
                .applyStartAt(applyEndAt.minusDays(10))
                .applyEndAt(applyEndAt)
                .summary("summary")
                .applicationMethod("online")
                .applicationUrl("https://example.com/apply")
                .imageUrl("https://example.com/image.png")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("이메일 발송에 성공하면 SUCCESS 이력을 저장한다")
    void sendDeadlineReminderEmails_saveSuccessLog() {
        LocalDate today = LocalDate.of(2026, 7, 18);
        User user = User.builder().id(1L).email("user@example.com").build();
        Post post = createPost(1L, today.plusDays(7));

        DeadlineEmailReminderTarget target =
                new DeadlineEmailReminderTarget(user, post, "notify@example.com");

        when(userSaveRepository.findDeadlineEmailReminderTargets(anyList()))
                .thenReturn(List.of(target));

        when(deadlineEmailNotificationLogRepository.existsByUserAndPostAndReminderTypeAndApplyEndAt(
                eq(user),
                eq(post),
                eq(DeadlineReminderType.D_MINUS_7),
                eq(post.getApplyEndAt())
        )).thenReturn(false);

        deadlineEmailNotificationService.sendDeadlineReminderEmails(today);

        verify(deadlineEmailSender)
                .send("notify@example.com", post, DeadlineReminderType.D_MINUS_7);

        ArgumentCaptor<DeadlineEmailNotificationLog> captor =
                ArgumentCaptor.forClass(DeadlineEmailNotificationLog.class);

        verify(deadlineEmailNotificationLogRepository).save(captor.capture());

        DeadlineEmailNotificationLog log = captor.getValue();

        assertSame(user, log.getUser());
        assertSame(post, log.getPost());
        assertEquals(DeadlineReminderType.D_MINUS_7, log.getReminderType());
        assertEquals(post.getApplyEndAt(), log.getApplyEndAt());
        assertEquals(EmailSendStatus.SUCCESS, log.getStatus());
    }

    @Test
    @DisplayName("일부 이메일 발송에 실패해도 실패 이력을 저장하고 다음 대상 발송을 계속한다")
    void sendDeadlineReminderEmails_continueAfterFailure() {
        LocalDate today = LocalDate.of(2026, 7, 18);

        User firstUser = User.builder().id(1L).email("first@example.com").build();
        Post firstPost = createPost(1L, today.plusDays(7));
        DeadlineEmailReminderTarget firstTarget =
                new DeadlineEmailReminderTarget(firstUser, firstPost, "first-notify@example.com");

        User secondUser = User.builder().id(2L).email("second@example.com").build();
        Post secondPost = createPost(2L, today.plusDays(3));
        DeadlineEmailReminderTarget secondTarget =
                new DeadlineEmailReminderTarget(secondUser, secondPost, "second-notify@example.com");

        when(userSaveRepository.findDeadlineEmailReminderTargets(anyList()))
                .thenReturn(List.of(firstTarget, secondTarget));

        when(deadlineEmailNotificationLogRepository.existsByUserAndPostAndReminderTypeAndApplyEndAt(
                any(User.class),
                any(Post.class),
                any(DeadlineReminderType.class),
                any(LocalDate.class)
        )).thenReturn(false);

        doThrow(new RuntimeException("smtp failed"))
                .doNothing()
                .when(deadlineEmailSender)
                .send(anyString(), any(Post.class), any(DeadlineReminderType.class));

        deadlineEmailNotificationService.sendDeadlineReminderEmails(today);

        verify(deadlineEmailSender, times(2))
                .send(anyString(), any(Post.class), any(DeadlineReminderType.class));

        ArgumentCaptor<DeadlineEmailNotificationLog> captor =
                ArgumentCaptor.forClass(DeadlineEmailNotificationLog.class);

        verify(deadlineEmailNotificationLogRepository, times(2)).save(captor.capture());

        List<DeadlineEmailNotificationLog> logs = captor.getAllValues();

        assertEquals(EmailSendStatus.FAILED, logs.get(0).getStatus());
        assertEquals(DeadlineReminderType.D_MINUS_7, logs.get(0).getReminderType());
        assertEquals("smtp failed", logs.get(0).getErrorMessage());

        assertEquals(EmailSendStatus.SUCCESS, logs.get(1).getStatus());
        assertEquals(DeadlineReminderType.D_MINUS_3, logs.get(1).getReminderType());
    }
}
