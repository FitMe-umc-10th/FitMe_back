package umc.fitme.domain.notify.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import umc.fitme.domain.notify.dto.AppliedPostReminderTarget;
import umc.fitme.domain.notify.dto.DeadlineEmailReminderTarget;
import umc.fitme.domain.notify.dto.DeadlineNotificationTarget;
import umc.fitme.domain.notify.entity.Notification;
import umc.fitme.domain.notify.enums.DeadlineReminderType;
import umc.fitme.domain.notify.enums.NotificationType;
import umc.fitme.domain.notify.repository.AppliedPostReminderRepository;
import umc.fitme.domain.notify.repository.NotificationRepository;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.repository.UserSaveRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlineNotificationService {

    private static final int MAX_TEXT_LENGTH = 255;

    private final UserSaveRepository userSaveRepository;
    private final AppliedPostReminderRepository appliedPostReminderRepository;
    private final NotificationRepository notificationRepository;

    public void createDeadlineNotifications(LocalDate today) {
        List<LocalDate> applyEndDates = Arrays.stream(DeadlineReminderType.values())
                .map(type -> today.plusDays(type.getDaysBefore()))
                .toList();

        for (DeadlineNotificationTarget target : collectTargets(applyEndDates)) {

            try {
                createIfAbsent(target, today);
            } catch (Exception e) {
                log.warn(
                        "마감 임박 알림 생성에 실패했습니다. userId={}, postId={}",
                        target.user() != null ? target.user().getId() : null,
                        target.post() != null ? target.post().getId() : null,
                        e
                );
            }
        }
    }

    private List<DeadlineNotificationTarget> collectTargets(List<LocalDate> applyEndDates) {
        Map<TargetKey, DeadlineNotificationTarget> merged = new LinkedHashMap<>();

        for (DeadlineEmailReminderTarget saved
                : userSaveRepository.findDeadlineEmailReminderTargets(applyEndDates)) {
            putIfIdentifiable(merged, DeadlineNotificationTarget.saved(saved.user(), saved.post()));
        }

        for (AppliedPostReminderTarget applied
                : appliedPostReminderRepository.findDeadlineReminderTargets(applyEndDates)) {
            putIfIdentifiable(merged, DeadlineNotificationTarget.applied(applied.user(), applied.post()));
        }

        return new ArrayList<>(merged.values());
    }

    private void putIfIdentifiable(
            Map<TargetKey, DeadlineNotificationTarget> merged,
            DeadlineNotificationTarget target
    ) {
        User user = target.user();
        Post post = target.post();

        if (user == null || user.getId() == null || post == null || post.getId() == null) {
            return;
        }

        merged.put(new TargetKey(user.getId(), post.getId()), target);
    }

    private void createIfAbsent(DeadlineNotificationTarget target, LocalDate today) {
        User user = target.user();
        Post post = target.post();

        DeadlineReminderType reminderType = resolveReminderType(today, post.getApplyEndAt());
        if (reminderType == null) {
            return;
        }

        boolean alreadyCreated = notificationRepository.existsSince(
                user.getId(),
                post.getId(),
                NotificationType.LAST_MINUTE,
                today.atStartOfDay()
        );
        if (alreadyCreated) {
            return;
        }

        notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .post(post)
                        .title(truncate(createTitle(post)))
                        .message(truncate(createMessage(reminderType, target.applied())))
                        .notificationType(NotificationType.LAST_MINUTE)
                        .build()
        );
    }

    private DeadlineReminderType resolveReminderType(LocalDate today, LocalDate applyEndAt) {
        if (applyEndAt == null) {
            return null;
        }

        long daysBefore = ChronoUnit.DAYS.between(today, applyEndAt);

        return Arrays.stream(DeadlineReminderType.values())
                .filter(type -> type.getDaysBefore() == daysBefore)
                .findFirst()
                .orElse(null);
    }

    private String createTitle(Post post) {
        return post.getTitle();
    }

    private String createMessage(DeadlineReminderType reminderType, boolean applied) {
        int daysBefore = reminderType.getDaysBefore();

        return applied
                ? "마감일이 %d일 남았습니다. 지원 내용을 다시 확인해 보세요.".formatted(daysBefore)
                : "마감일이 %d일 남았습니다. 잊지 말고 지원하세요!".formatted(daysBefore);
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }

        return value.length() > MAX_TEXT_LENGTH
                ? value.substring(0, MAX_TEXT_LENGTH)
                : value;
    }

    private record TargetKey(Long userId, Long postId) {
    }
}
