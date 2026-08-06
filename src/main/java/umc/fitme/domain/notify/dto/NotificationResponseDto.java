package umc.fitme.domain.notify.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationResponseDto {

    @Builder
    public record NotificationListResponse(
            Boolean hasNext,
            Long nextCursor,
            List<NotificationItem> notifications
    ) {
    }

    @Builder
    public record NotificationItem(
            Long notificationId,
            String type,
            String categoryPrefix,
            String title,
            String message,
            LocalDateTime createdAt,
            String displayTime,
            Boolean isRead,
            Long postId,
            String postType
    ) {
    }

    @Builder
    public record UnreadCountResponse(
            Long unreadCount
    ) {
    }
}
