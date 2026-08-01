package umc.fitme.domain.notify.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.fitme.domain.notify.service.DeadlineEmailNotificationService;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Profile("ec2")
public class DeadlineEmailNotificationScheduler {

    private final DeadlineEmailNotificationService deadlineEmailNotificationService;
    private final Clock clock;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendDeadlineReminderEmails() {
        LocalDate today = LocalDate.now(clock);
        deadlineEmailNotificationService.sendDeadlineReminderEmails(today);
    }
}
