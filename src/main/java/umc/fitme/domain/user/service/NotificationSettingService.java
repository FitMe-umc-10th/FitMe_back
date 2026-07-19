package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.user.dto.NotificationSettingRequestDto;
import umc.fitme.domain.user.dto.NotificationSettingResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserNotificationSetting;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserNotificationSettingRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    private final UserRepository userRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;

    /**
     * 알림 설정을 조회합니다. 설정이 아직 없으면 기본값(토글 전체 false)으로 생성해 반환합니다.
     *
     * @param userId 조회 대상(로그인한) 사용자 식별자
     * @return 알림 설정 정보
     */
    @Transactional
    public NotificationSettingResponseDto.NotificationSettingResponse getMyNotificationSetting(Long userId) {
        User user = getActiveUser(userId);

        return NotificationSettingResponseDto.NotificationSettingResponse.from(getOrCreateSetting(user));
    }

    /**
     * 알림 설정을 부분 수정합니다. (값이 오는 필드만 병합)
     *
     * @param userId 수정 대상(로그인한) 사용자 식별자
     * @param request 부분 수정 요청(모든 필드 nullable)
     * @return 수정된 알림 설정 정보
     */
    @Transactional
    public NotificationSettingResponseDto.NotificationSettingResponse updateMyNotificationSetting(
            Long userId, NotificationSettingRequestDto.UpdateNotificationSettingRequest request) {

        // 부분수정인데 아무 필드도 오지 않으면 수정할 항목이 없음
        if (request.notificationEmail() == null
                && request.pushEnabled() == null
                && request.recommendedEnabled() == null
                && request.reminderEnabled() == null) {
            throw new ProjectException(UserErrorCode.NOTIFICATION_UPDATE_EMPTY);
        }

        User user = getActiveUser(userId);

        UserNotificationSetting setting = getOrCreateSetting(user);

        // 값이 온 필드만 반영 (엔티티 update 메서드가 null 인자를 무시)
        setting.updateNotificationEmail(request.notificationEmail());
        setting.updatePushEnabled(request.pushEnabled());
        setting.updateRecommendedEnabled(request.recommendedEnabled());
        setting.updateReminderEnabled(request.reminderEnabled());

        return NotificationSettingResponseDto.NotificationSettingResponse.from(setting);
    }

    /**
     * 탈퇴하지 않은 사용자를 조회합니다.
     *
     * @param userId 대상 사용자 식별자
     * @return 탈퇴하지 않은 사용자
     */
    private User getActiveUser(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * 사용자의 알림 설정을 조회하고, 없으면 기본 설정을 만들어 저장합니다.
     * 조회/수정 양쪽이 동일한 초기화 규칙을 쓰도록 재사용합니다.
     *
     * @param user 대상 사용자
     * @return 영속 상태의 알림 설정
     */
    private UserNotificationSetting getOrCreateSetting(User user) {
        return userNotificationSettingRepository.findByUser(user)
                .orElseGet(() -> userNotificationSettingRepository.save(
                        UserNotificationSetting.createDefault(user, user.getEmail())));
    }
}