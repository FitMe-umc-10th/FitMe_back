package umc.fitme.domain.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import umc.fitme.domain.post.util.ScholarshipAmountParser;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("scholarship")
public class Scholarship extends Post{

    @Column(name = "grade_requirement")
    private String gradeRequirement;

    @Column(name = "income_requirement")
    private String incomeRequirement;

    @Column(name = "region_requirement")
    private String regionRequirement;

    @Column(name = "university_requirement")
    private String universityRequirement;

    @Column(name = "support_amount")
    private String supportAmount;

    @Column(name = "support_amount_value")
    private Long supportAmountValue;

    @Column(name = "source_key", unique = true)
    private String sourceKey;

    @Column(name = "active", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 1")
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @PrePersist
    @PreUpdate
    private void updateSupportAmountValue() {
        this.supportAmountValue = ScholarshipAmountParser.parse(supportAmount);
    }

    public void syncFrom(
            String title,
            String organizer,
            LocalDate applyStartAt,
            LocalDate applyEndAt,
            String summary,
            String applicationMethod,
            String applicationUrl,
            String supportAmount,
            LocalDateTime now
    ) {
        updateCore(title, organizer, applyStartAt, applyEndAt, summary, applicationMethod, applicationUrl, now);
        this.supportAmount = supportAmount;
        this.active = true;
        this.lastSyncedAt = now;
    }

    public void deactivate(LocalDateTime now) {
        this.active = false;
        this.lastSyncedAt = now;
    }
}
