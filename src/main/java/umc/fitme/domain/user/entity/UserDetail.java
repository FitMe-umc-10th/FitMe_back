package umc.fitme.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "user_detail")
public class UserDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "region", nullable = false)
    private String region;

    @Column(name = "university_name", nullable = false)
    private String universityName;

    @Column(name = "gpa", nullable = false)
    private Float gpa;

    @Column(name = "income_bracket", nullable = false)
    private int incomeBracket;
}
