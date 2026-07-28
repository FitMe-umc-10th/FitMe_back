package umc.fitme.domain.post.sync.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import umc.fitme.domain.post.sync.service.ScholarshipSyncService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScholarshipSyncSchedulerTest {

    @Test
    @DisplayName("스케줄러 실행 시 장학금 동기화 서비스를 호출한다")
    void syncScholarships_callsService() {
        ScholarshipSyncService service = mock(ScholarshipSyncService.class);
        ScholarshipSyncScheduler scheduler = new ScholarshipSyncScheduler(service);

        scheduler.syncScholarships();

        verify(service).sync();
    }
}
