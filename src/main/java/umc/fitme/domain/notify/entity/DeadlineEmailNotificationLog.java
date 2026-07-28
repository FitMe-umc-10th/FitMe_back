package umc.fitme.domain.notify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import umc.fitme.domain.notify.enums.DeadlineReminderType;
import umc.fitme.domain.notify.enums.EmailSendStatus;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;
import umc.fitme.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(
        name = "deadline_email_notification_log",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_deadline_email_notification_log",
                columnNames = {"user_id", "post_id", "reminder_type", "apply_end_at"}
                )
        }
)
public class DeadlineEmailNotificationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false)
    private DeadlineReminderType reminderType;

    @Column(name = "apply_end_at", nullable = false)
    private LocalDate applyEndAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmailSendStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public static DeadlineEmailNotificationLog success(
            User user,
            Post post,
            DeadlineReminderType reminderType
    ) {
        return DeadlineEmailNotificationLog.builder()
                .user(user)
                .post(post)
                .reminderType(reminderType)
                .applyEndAt(post.getApplyEndAt())
                .status(EmailSendStatus.SUCCESS)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static DeadlineEmailNotificationLog failed(
            User user,
            Post post,
            DeadlineReminderType reminderType,
            String errorMessage
    ) {
        return DeadlineEmailNotificationLog.builder()
                .user(user)
                .post(post)
                .reminderType(reminderType)
                .applyEndAt(post.getApplyEndAt())
                .status(EmailSendStatus.FAILED)
                .errorMessage(errorMessage)
                .sentAt(LocalDateTime.now())
                .build();
    }
}
