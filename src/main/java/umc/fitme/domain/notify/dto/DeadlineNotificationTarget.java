package umc.fitme.domain.notify.dto;

import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;

public record DeadlineNotificationTarget(
        User user,
        Post post,
        boolean applied
) {

    public static DeadlineNotificationTarget saved(User user, Post post) {
        return new DeadlineNotificationTarget(user, post, false);
    }

    public static DeadlineNotificationTarget applied(User user, Post post) {
        return new DeadlineNotificationTarget(user, post, true);
    }
}
