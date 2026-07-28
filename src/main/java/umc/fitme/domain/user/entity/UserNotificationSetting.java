package umc.fitme.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import umc.fitme.global.entity.BaseEntity;

@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "user_notification_setting")
public class UserNotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "notification_email", nullable = false)
    private String notificationEmail;

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private Boolean pushEnabled = false;

    @Column(name = "recommended_enabled", nullable = false)
    @Builder.Default
    private Boolean recommendedEnabled = false;

    @Column(name = "reminder_enabled", nullable = false)
    @Builder.Default
    private Boolean reminderEnabled = false;

    public static UserNotificationSetting createDefault(User user, String notificationEmail) {
        return UserNotificationSetting.builder()
                .user(user)
                .notificationEmail(notificationEmail)
                .pushEnabled(false)
                .recommendedEnabled(false)
                .reminderEnabled(false)
                .build();
    }

    public void updateNotificationEmail(String notificationEmail) {
        if (notificationEmail != null) {
            this.notificationEmail = notificationEmail;
        }
    }

    public void updatePushEnabled(Boolean pushEnabled) {
        if (pushEnabled != null) {
            this.pushEnabled = pushEnabled;
        }
    }

    public void updateRecommendedEnabled(Boolean recommendedEnabled) {
        if (recommendedEnabled != null) {
            this.recommendedEnabled = recommendedEnabled;
        }
    }

    public void updateReminderEnabled(Boolean reminderEnabled) {
        if (reminderEnabled != null) {
            this.reminderEnabled = reminderEnabled;
        }
    }

    /**
     * 마스터 스위치(pushEnabled)가 꺼진 상태면 하위 알림도 강제로 끈다.
     */
    public void enforcePushCascade() {
        if (Boolean.FALSE.equals(this.pushEnabled)) {
            this.recommendedEnabled = false;
            this.reminderEnabled = false;
        }
    }
}
