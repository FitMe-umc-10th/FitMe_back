package umc.fitme.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import umc.fitme.domain.auth.exception.AuthException;
import umc.fitme.domain.auth.exception.code.AuthErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}") private String fromAddress;

    public void sendVerificationCode(String email, String code){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("[FitMe] 이메일 인증번호 안내");
            helper.setText(buildContent(code), true);

            mailSender.send(message);
            log.info("인증 메일 발송 성공, to = {}", email);
        } catch (MessagingException e) {
            throw new AuthException(AuthErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String buildContent(String code) {
        return """
                <div style="font-family:'Apple SD Gothic Neo', sans-serif; padding:24px;">
                    <h2>FitMe 이메일 인증</h2>
                    <p>아래 인증번호를 <b>5분 이내</b>에 입력해 주세요.</p>
                    <div style="font-size:32px; font-weight:bold; letter-spacing:6px; margin:16px 0;">
                        %s
                    </div>
                    <p style="color:#888; font-size:12px;">본 메일은 회원가입 이메일 인증을 위해 발송되었습니다.</p>
                </div>
                """.formatted(code);
    }
}
