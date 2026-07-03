package umc.fitme.domain.post.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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
