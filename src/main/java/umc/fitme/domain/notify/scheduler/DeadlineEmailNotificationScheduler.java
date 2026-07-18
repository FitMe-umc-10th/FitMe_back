package umc.fitme.domain.notify.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.fitme.domain.notify.service.DeadlineEmailNotificationService;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class DeadlineEmailNotificationScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final DeadlineEmailNotificationService deadlineEmailNotificationService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul") // 매일 오전 9시에 실행
    public void sendDeadlineReminderEmails() {
        LocalDate today = LocalDate.now(KOREA_ZONE);
        deadlineEmailNotificationService.sendDeadlineReminderEmails(today);
    }
}
