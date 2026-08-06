package umc.fitme.domain.notify.converter;

import umc.fitme.domain.notify.dto.NotificationResponseDto;
import umc.fitme.domain.notify.entity.Notification;
import umc.fitme.domain.notify.enums.NotificationType;
import umc.fitme.domain.notify.util.RelativeTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationConverter {

    public static String toResponseType(NotificationType notificationType) {
        if (notificationType == null) {
            return null;
        }
        return switch (notificationType) {
            case LAST_MINUTE -> "DEADLINE";
            case APPLY_MANAGE -> "APPLICATION";
        };
    }

    public static String toCategoryPrefix(NotificationType notificationType) {
        if (notificationType == null) {
            return null;
        }
        return switch (notificationType) {
            case LAST_MINUTE -> "[마감 임박]";
            case APPLY_MANAGE -> "[지원 관리]";
        };
    }

    public static NotificationResponseDto.NotificationItem toItem(Notification notification, LocalDateTime now) {
        NotificationType type = notification.getNotificationType();

        return NotificationResponseDto.NotificationItem.builder()
                .notificationId(notification.getId())
                .type(toResponseType(type))
                .categoryPrefix(toCategoryPrefix(type))
                .title(notification.getTitle())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .displayTime(notification.getCreatedAt() != null
                        ? RelativeTimeFormatter.format(notification.getCreatedAt(), now)
                        : null)
                .isRead(notification.getIsRead())
                .postId(notification.getPost() != null ? notification.getPost().getId() : null)
                .postType(notification.getPost() != null && notification.getPost().getPostType() != null
                        ? notification.getPost().getPostType().name()
                        : null)
                .build();
    }

    public static NotificationResponseDto.NotificationListResponse toListResponse(
            List<Notification> notifications,
            boolean hasNext,
            LocalDateTime now
    ) {
        List<NotificationResponseDto.NotificationItem> items = notifications.stream()
                .map(notification -> toItem(notification, now))
                .toList();

        Long nextCursor = items.isEmpty() ? null : items.getLast().notificationId();

        return NotificationResponseDto.NotificationListResponse.builder()
                .hasNext(hasNext)
                .nextCursor(hasNext ? nextCursor : null)
                .notifications(items)
                .build();
    }

    public static NotificationResponseDto.UnreadCountResponse toUnreadCountResponse(long unreadCount) {
        return NotificationResponseDto.UnreadCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }
}
