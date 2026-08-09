package umc.fitme.domain.post.sync.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.fitme.domain.post.sync.service.ScholarshipSyncService;

@Component
@RequiredArgsConstructor
@Profile("ec2")
public class ScholarshipSyncScheduler {

    private final ScholarshipSyncService scholarshipSyncService;

    /***
     * 스케줄러 기능: 매월 1일 새벽 4시에 장학금 공공데이터를 동기화한다.
     */
    @Scheduled(cron = "0 0 4 1 * *", zone = "Asia/Seoul")
    public void syncScholarships() {
        scholarshipSyncService.sync();
    }
}
