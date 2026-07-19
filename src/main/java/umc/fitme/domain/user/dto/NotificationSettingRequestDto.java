package umc.fitme.domain.user.dto;

import jakarta.validation.constraints.Email;

public class NotificationSettingRequestDto {

    /* PATCH 알림 설정 부분수정 요청 (모든 필드 nullable) */
    public record UpdateNotificationSettingRequest(
            @Email(message = "올바르지 않은 이메일 형식입니다.")
            String notificationEmail,

            Boolean pushEnabled,

            Boolean recommendedEnabled,

            Boolean reminderEnabled
    ) {
    }
}