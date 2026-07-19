package umc.fitme.domain.notify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import umc.fitme.domain.notify.scheduler.DeadlineEmailNotificationScheduler;
import umc.fitme.domain.notify.service.DeadlineEmailNotificationService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeadlineEmailNotificationSchedulerTest {

    @Test
    @DisplayName("스케줄러 실행 시 Asia/Seoul 기준 오늘 날짜로 마감일 이메일 알림 서비스를 호출한다")
    void sendDeadlineReminderEmails_callServiceWithToday() {
        DeadlineEmailNotificationService service = mock(DeadlineEmailNotificationService.class);
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-07-18T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        DeadlineEmailNotificationScheduler scheduler =
                new DeadlineEmailNotificationScheduler(service, fixedClock);

        scheduler.sendDeadlineReminderEmails();

        verify(service).sendDeadlineReminderEmails(LocalDate.of(2026, 7, 18));
    }
}
