package umc.fitme.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import umc.fitme.domain.user.enums.SocialType;
import umc.fitme.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "social_uid")
    private String socialUid;

    @Column(name = "social_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SocialType socialType = SocialType.NULL;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "terms_agreed")
    private Boolean termsAgreed;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_onboarded")
    @Builder.Default
    private Boolean isOnboarded = false;

    @Column(name = "apply_count")
    @Builder.Default
    private int applyCount = 0;

    public void completeOnboarding() {
        this.isOnboarded = true;
    }

    // 계정 연동
    public void linkAccount(SocialType socialType, String socialUid) {
        this.socialType = socialType;
        this.socialUid = socialUid;
    }

    // 회원 탈퇴 및 이메일 더미데이터로 덮어버림
    public void deleteUser(){
        this.email = "deleted_" + System.currentTimeMillis() + "_" + email;
        this.deletedAt = LocalDateTime.now();
    }
}
