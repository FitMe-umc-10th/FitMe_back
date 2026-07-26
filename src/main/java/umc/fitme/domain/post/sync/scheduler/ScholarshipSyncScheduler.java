package umc.fitme.domain.post.sync.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.fitme.domain.post.sync.service.ScholarshipSyncService;

@Component
@RequiredArgsConstructor
public class ScholarshipSyncScheduler {

    private final ScholarshipSyncService scholarshipSyncService;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void syncScholarships() {
        scholarshipSyncService.sync();
    }
}
