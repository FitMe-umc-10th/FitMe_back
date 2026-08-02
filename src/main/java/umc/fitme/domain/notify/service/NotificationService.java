package umc.fitme.domain.notify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.notify.converter.NotificationConverter;
import umc.fitme.domain.notify.dto.NotificationResponseDto;
import umc.fitme.domain.notify.entity.Notification;
import umc.fitme.domain.notify.repository.NotificationRepository;

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

    @Transactional
    public NotificationResponseDto.NotificationListResponse getNotifications(
            Long userId,
            Long cursor,
            Integer size
    ) {
        int normalizedSize = Math.clamp(size == null ? DEFAULT_SIZE : size, 1, MAX_SIZE);
        boolean firstPage = (cursor == null || cursor <= 0);
        long normalizedCursor = firstPage ? Long.MAX_VALUE : cursor;

        List<Notification> found = notificationRepository.findAllByUserIdBeforeCursor(
                userId,
                normalizedCursor,
                PageRequest.of(0, normalizedSize + 1)
        );

        boolean hasNext = found.size() > normalizedSize;
        List<Notification> notifications = hasNext ? found.subList(0, normalizedSize) : found;

        NotificationResponseDto.NotificationListResponse response =
                NotificationConverter.toListResponse(notifications, hasNext, LocalDateTime.now(clock));

        if (firstPage) {
            notificationRepository.markAllAsRead(userId);
        }

        return response;
    }

    public NotificationResponseDto.UnreadCountResponse getUnreadCount(Long userId) {
        return NotificationConverter.toUnreadCountResponse(
                notificationRepository.countByUserIdAndIsReadFalse(userId)
        );
    }
}
