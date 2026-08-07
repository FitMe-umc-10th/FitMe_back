package umc.fitme.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

    // 특정 대학 전용 공고인지 판별하기 위한 조건. 전국/제한 없음이면 모든 대학이 통과한다.
    @Column(name = "university_requirement")
    private String universityRequirement;

    // 표시용 원본 문자열 금액
    @Column(name = "support_amount", nullable = false)
    private String supportAmount;

    // 저장 시 supportAmount를 파싱해 미리 계산해 두는 숫자 금액 (조회 시 재파싱 방지)
    @Column(name = "support_amount_value")
    private Long supportAmountValue;

    // 한국장학재단 CSV 원본 row를 식별하기 위한 키 (운영기관+상품명+상품구분+신청기간 조합)
    @Column(name = "source_key", unique = true)
    private String sourceKey;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @PrePersist
    @PreUpdate
    private void updateSupportAmountValue() {
        this.supportAmountValue = ScholarshipAmountParser.parse(supportAmount);
    }

    /***
     * 함수 기능: CSV 동기화 시 기존 장학금 데이터를 최신 내용으로 갱신 처리한다.
     * gradeRequirement/incomeRequirement/regionRequirement는 CSV 원본 컬럼과의
     * 정확한 매핑이 확인되지 않아 이번 동기화에서는 건드리지 않는다(null로 덮어쓰지 않음).
     */
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
        // CSV에 여전히 남아있어도 마감일이 지난 장학금은 다시 활성화하지 않는다.
        // (PostExpirationScheduler가 비활성화한 걸 다음 동기화가 무조건 되살리던 버그)
        if (applyEndAt != null && applyEndAt.isBefore(now.toLocalDate())) {
            this.deactivate();
        } else {
            this.activate();
        }
        this.lastSyncedAt = now;
    }

    /***
     * 함수 기능: 이번 동기화 대상 CSV에 더 이상 존재하지 않는 장학금을 비활성화 처리한다.
     */
    public void deactivate(LocalDateTime now) {
        this.deactivate();
        this.lastSyncedAt = now;
    }
}
