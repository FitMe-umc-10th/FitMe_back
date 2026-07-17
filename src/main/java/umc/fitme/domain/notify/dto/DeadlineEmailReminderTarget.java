package umc.fitme.domain.notify.dto;

import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;

public record DeadlineEmailReminderTarget(
        User user,
        Post post,
        String notificationEmail
) {
}