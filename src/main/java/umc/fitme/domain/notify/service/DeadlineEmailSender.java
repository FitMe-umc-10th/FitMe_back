package umc.fitme.domain.notify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import umc.fitme.domain.notify.enums.DeadlineReminderType;
import umc.fitme.domain.post.entity.Post;

@Service
@RequiredArgsConstructor
public class DeadlineEmailSender {

    private final JavaMailSender javaMailSender;

    public void send(String to, Post post, DeadlineReminderType reminderType) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(createSubject(post, reminderType));
        message.setText(createBody(post, reminderType));

        javaMailSender.send(message);
    }

    private String createSubject(Post post, DeadlineReminderType reminderType) {
        return "[FitMe] 관심 공고 마감 " + reminderType.getDaysBefore() + "일 전 알림";
    }

    private String createBody(Post post, DeadlineReminderType reminderType) {
        return """
                관심 공고 마감일이 %d일 남았습니다.
                
                공고명: %s
                기관명: %s
                마감일: %s
                신청 링크: %s
                """.formatted(
                reminderType.getDaysBefore(),
                post.getTitle(),
                post.getOrganizer(),
                post.getApplyEndAt(),
                post.getApplicationUrl()
        );
    }
}
