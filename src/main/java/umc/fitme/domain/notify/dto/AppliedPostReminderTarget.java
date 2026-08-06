package umc.fitme.domain.notify.dto;

import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;

public record AppliedPostReminderTarget(
        User user,
        Post post
) {
}
