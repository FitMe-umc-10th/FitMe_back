package umc.fitme.domain.notify.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import umc.fitme.domain.notify.dto.DeadlineEmailReminderTarget;
import umc.fitme.domain.notify.entity.DeadlineEmailNotificationLog;
import umc.fitme.domain.notify.enums.DeadlineReminderType;
import umc.fitme.domain.notify.repository.DeadlineEmailNotificationLogRepository;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.repository.UserSaveRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlineEmailNotificationService {

    private final UserSaveRepository userSaveRepository;
    private final DeadlineEmailNotificationLogRepository deadlineEmailNotificationLogRepository;
    private final DeadlineEmailSender deadlineEmailSender;

    public void sendDeadlineReminderEmails(LocalDate today) {
        List<LocalDate> applyEndDates = Arrays.stream(DeadlineReminderType.values())
                .map(type -> today.plusDays(type.getDaysBefore()))
                .toList();

        List<DeadlineEmailReminderTarget> targets =
                userSaveRepository.findDeadlineEmailReminderTargets(applyEndDates);

        for (DeadlineEmailReminderTarget target : targets) {
            // 한 대상의 실패가 남은 대상의 발송까지 막지 않도록 개별적으로 격리한다.
            try {
                sendIfNeeded(target, today);
            } catch (Exception e) {
                log.warn(
                        "마감 임박 메일 처리에 실패했습니다. userId={}, postId={}",
                        target.user() != null ? target.user().getId() : null,
                        target.post() != null ? target.post().getId() : null,
                        e
                );
            }
        }
    }

    private void sendIfNeeded(DeadlineEmailReminderTarget target, LocalDate today) {
        User user = target.user();
        Post post = target.post();
        DeadlineReminderType reminderType = resolveReminderType(today, post.getApplyEndAt());

        if (reminderType == null) {
            return;
        }

        boolean alreadySent =
                deadlineEmailNotificationLogRepository.existsByUserAndPostAndReminderTypeAndApplyEndAt(
                        user,
                        post,
                        reminderType,
                        post.getApplyEndAt()
                );

        if (alreadySent) {
            return;
        }

        try {
            deadlineEmailSender.send(target.notificationEmail(), post, reminderType);

            deadlineEmailNotificationLogRepository.save(
                    DeadlineEmailNotificationLog.success(user, post, reminderType)
            );
        } catch (Exception e) {
            log.warn(
                    "마감 임박 메일 발송에 실패했습니다. userId={}, postId={}, reminderType={}",
                    user.getId(), post.getId(), reminderType, e
            );

            deadlineEmailNotificationLogRepository.save(
                    DeadlineEmailNotificationLog.failed(
                            user,
                            post,
                            reminderType,
                            truncateErrorMessage(e.getMessage())
                    )
            );
        }
    }

    private DeadlineReminderType resolveReminderType(LocalDate today, LocalDate applyEndAt) {
        long daysBefore = ChronoUnit.DAYS.between(today, applyEndAt);

        return Arrays.stream(DeadlineReminderType.values())
                .filter(type -> type.getDaysBefore() == daysBefore)
                .findFirst()
                .orElse(null);
    }

    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        return errorMessage.length() > 1000
                ? errorMessage.substring(0, 1000)
                : errorMessage;
    }
}