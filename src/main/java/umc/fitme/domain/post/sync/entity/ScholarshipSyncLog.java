package umc.fitme.domain.post.sync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.fitme.domain.post.sync.enums.ScholarshipSyncStatus;
import umc.fitme.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "scholarship_sync_log")
public class ScholarshipSyncLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "inserted_count", nullable = false)
    private int insertedCount;

    @Column(name = "updated_count", nullable = false)
    private int updatedCount;

    @Column(name = "inactivated_count", nullable = false)
    private int inactivatedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScholarshipSyncStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    public static ScholarshipSyncLog success(
            int totalCount,
            int insertedCount,
            int updatedCount,
            int inactivatedCount
    ) {
        return ScholarshipSyncLog.builder()
                .totalCount(totalCount)
                .insertedCount(insertedCount)
                .updatedCount(updatedCount)
                .inactivatedCount(inactivatedCount)
                .status(ScholarshipSyncStatus.SUCCESS)
                .syncedAt(LocalDateTime.now())
                .build();
    }

    public static ScholarshipSyncLog failed(String errorMessage) {
        return ScholarshipSyncLog.builder()
                .totalCount(0)
                .insertedCount(0)
                .updatedCount(0)
                .inactivatedCount(0)
                .status(ScholarshipSyncStatus.FAILED)
                .errorMessage(errorMessage)
                .syncedAt(LocalDateTime.now())
                .build();
    }
}
