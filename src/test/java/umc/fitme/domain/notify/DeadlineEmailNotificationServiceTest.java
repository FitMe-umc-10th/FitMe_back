package umc.fitme.domain.notify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.notify.repository.DeadlineEmailNotificationLogRepository;
import umc.fitme.domain.notify.service.DeadlineEmailNotificationService;
import umc.fitme.domain.notify.service.DeadlineEmailSender;
import umc.fitme.domain.user.repository.UserSaveRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
