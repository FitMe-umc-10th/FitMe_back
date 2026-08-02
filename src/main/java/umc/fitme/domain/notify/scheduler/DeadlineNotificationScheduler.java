package umc.fitme.domain.notify.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.fitme.domain.notify.service.DeadlineNotificationService;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DeadlineNotificationScheduler {

    private final DeadlineNotificationService deadlineNotificationService;
    private final Clock clock;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void createDeadlineNotifications() {
        LocalDate today = LocalDate.now(clock);
        deadlineNotificationService.createDeadlineNotifications(today);
    }
}
