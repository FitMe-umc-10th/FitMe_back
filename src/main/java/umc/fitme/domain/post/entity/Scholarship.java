package umc.fitme.domain.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import umc.fitme.domain.post.util.ScholarshipAmountParser;

@Entity
@Getter
@DiscriminatorValue("scholarship")
public class Scholarship extends Post{

    @Column(name = "grade_requirement")
    private String gradeRequirement;

    @Column(name = "income_requirement")
    private String incomeRequirement;

    @Column(name = "region_requirement")
    private String regionRequirement;

    // 표시용 원본 문자열 금액
    @Column(name = "support_amount")
    private String supportAmount;

    // 저장 시 supportAmount를 파싱해 미리 계산해 두는 숫자 금액 (조회 시 재파싱 방지)
    @Column(name = "support_amount_value")
    private Long supportAmountValue;

    @PrePersist
    @PreUpdate
    private void updateSupportAmountValue() {
        this.supportAmountValue = ScholarshipAmountParser.parse(supportAmount);
    }
}
