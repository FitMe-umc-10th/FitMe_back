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

    @Column(name = "support_amount", nullable = false)
    private String supportAmount;
}
