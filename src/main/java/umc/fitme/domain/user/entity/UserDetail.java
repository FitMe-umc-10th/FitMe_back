package umc.fitme.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import umc.fitme.global.entity.BaseEntity;

@Entity
@DynamicUpdate
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "user_detail")
public class UserDetail extends BaseEntity {

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

    @Column(name = "profile_image_url", nullable = true)
    private String profileImageUrl;

    /** 동시 수정 시 lost update 를 막기 위한 낙관적 락 버전 */
    @Version
    @Column(name = "version")
    private Long version;

    public void updateGpa(Float gpa) {
        this.gpa = gpa;
    }

    public void updateIncomeBracket(int incomeBracket) {
        this.incomeBracket = incomeBracket;
    }

    public void updateRegion(String region) {
        this.region = region;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
