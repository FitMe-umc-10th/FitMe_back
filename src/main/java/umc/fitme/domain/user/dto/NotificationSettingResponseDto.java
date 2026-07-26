package umc.fitme.domain.user.dto;

import umc.fitme.domain.user.entity.UserNotificationSetting;

public class NotificationSettingResponseDto {

    /* GET/PATCH 알림 설정 응답 */
    public record NotificationSettingResponse(
            String notificationEmail,
            Boolean pushEnabled,
            Boolean recommendedEnabled,
            Boolean reminderEnabled
    ) {
        public static NotificationSettingResponse from(UserNotificationSetting setting) {
            return new NotificationSettingResponse(
                    setting.getNotificationEmail(),
                    setting.getPushEnabled(),
                    setting.getRecommendedEnabled(),
                    setting.getReminderEnabled()
            );
        }
    }
}