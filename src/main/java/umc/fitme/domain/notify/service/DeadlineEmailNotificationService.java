package umc.fitme.domain.notify.service;

import lombok.RequiredArgsConstructor;
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
            sendIfNeeded(target, today);
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