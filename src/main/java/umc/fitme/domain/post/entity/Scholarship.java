package umc.fitme.domain.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor // 💡 추가
@AllArgsConstructor // 💡 추가
@DiscriminatorValue("scholarship")
public class Scholarship extends Post{

    @Column(name = "grade_requirement", nullable = false)
    private String gradeRequirement;

    @Column(name = "income_requirement", nullable = false)
    private String incomeRequirement;

    @Column(name = "region_requirement", nullable = false)
    private String regionRequirement;

    // 특정 대학 전용 공고인지 판별하기 위한 조건. 전국/제한 없음이면 모든 대학이 통과한다.
    @Column(name = "university_requirement")
    private String universityRequirement;

    @Column(name = "support_amount", nullable = false)
    private String supportAmount;
}
