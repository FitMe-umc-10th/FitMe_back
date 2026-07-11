package umc.fitme.domain.post.entity;

import jakarta.persistence.*;
import lombok.Getter;

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

    @Column(name = "support_amount")
    private String supportAmount;
}
