package umc.fitme.domain.post.sync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.sync.entity.ScholarshipSyncLog;

public interface ScholarshipSyncLogRepository extends JpaRepository<ScholarshipSyncLog, Long> {
}
