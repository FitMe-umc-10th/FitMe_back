package umc.fitme.domain.post.sync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.sync.entity.ScholarshipSyncLog;
import umc.fitme.domain.post.sync.enums.ScholarshipSyncStatus;

import java.util.Optional;

public interface ScholarshipSyncLogRepository extends JpaRepository<ScholarshipSyncLog, Long> {

    Optional<ScholarshipSyncLog> findTopByStatusOrderBySyncedAtDesc(ScholarshipSyncStatus status);
}
