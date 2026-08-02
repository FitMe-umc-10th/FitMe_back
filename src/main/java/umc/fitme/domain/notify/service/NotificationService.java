package umc.fitme.domain.notify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.notify.converter.NotificationConverter;
import umc.fitme.domain.notify.dto.NotificationResponseDto;
import umc.fitme.domain.notify.entity.Notification;
import umc.fitme.domain.notify.exception.code.NotificationErrorCode;
import umc.fitme.domain.notify.repository.NotificationRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final int DEFAULT_SIZE = 15;
    private static final int MAX_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public NotificationResponseDto.NotificationListResponse getNotifications(
            Long userId,
            Long cursor,
            Integer size
    ) {
        int normalizedSize = Math.clamp(size == null ? DEFAULT_SIZE : size, 1, MAX_SIZE);
        long normalizedCursor = (cursor == null || cursor <= 0) ? Long.MAX_VALUE : cursor;

        List<Notification> found = notificationRepository.findAllByUserIdBeforeCursor(
                userId,
                normalizedCursor,
                PageRequest.of(0, normalizedSize + 1)
        );

        boolean hasNext = found.size() > normalizedSize;
        List<Notification> notifications = hasNext ? found.subList(0, normalizedSize) : found;

        return NotificationConverter.toListResponse(notifications, hasNext, LocalDateTime.now(clock));
    }

    public NotificationResponseDto.UnreadCountResponse getUnreadCount(Long userId) {
        return NotificationConverter.toUnreadCountResponse(
                notificationRepository.countByUserIdAndIsReadFalse(userId)
        );
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ProjectException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getUser() == null
                || !notification.getUser().getId().equals(userId)) {
            throw new ProjectException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
        }

        notificationRepository.markAsRead(notificationId);
    }
}
