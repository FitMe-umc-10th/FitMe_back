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

    @Scheduled(cron = "0 0 4 1 * *", zone = "Asia/Seoul")
    public void syncScholarships() {
        scholarshipSyncService.sync();
    }
}
