package umc.fitme.domain.notify.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.fitme.domain.notify.service.DeadlineNotificationService;

import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineNotificationScheduler {

    private final DeadlineNotificationService deadlineNotificationService;
    private final Clock clock;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void createDeadlineNotifications() {
        LocalDate today = LocalDate.now(clock);

        // 잡 경계에서 실패를 잡아 맥락과 함께 남긴다.
        // 대상별 실패는 서비스 내부에서 격리되므로, 여기까지 오는 것은 대상 조회 자체의 실패다.
        try {
            deadlineNotificationService.createDeadlineNotifications(today);
        } catch (Exception e) {
            log.error("마감 임박 알림 생성 배치 실패 - date={}", today, e);
        }
    }
}
