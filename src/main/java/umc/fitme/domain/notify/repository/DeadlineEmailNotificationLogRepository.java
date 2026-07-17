package umc.fitme.domain.notify.repository;

import umc.fitme.domain.notify.enums.DeadlineReminderType;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;

import java.time.LocalDate;

public interface DeadlineEmailNotificationLogRepository {

    boolean existsByUserAndPostAndReminderTypeAndApplyEndAt(
            User user,
            Post post,
            DeadlineReminderType reminderType,
            LocalDate applyEndAt
    );
}
