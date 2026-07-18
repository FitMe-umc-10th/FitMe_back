package umc.fitme.domain.notify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.notify.entity.DeadlineEmailNotificationLog;
import umc.fitme.domain.notify.enums.DeadlineReminderType;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;

import java.time.LocalDate;

public interface DeadlineEmailNotificationLogRepository extends JpaRepository<DeadlineEmailNotificationLog, Long> {

    boolean existsByUserAndPostAndReminderTypeAndApplyEndAt(
            User user,
            Post post,
            DeadlineReminderType reminderType,
            LocalDate applyEndAt
    );
}
