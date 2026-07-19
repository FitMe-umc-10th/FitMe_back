package umc.fitme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.user.dto.NotificationSettingRequestDto;
import umc.fitme.domain.user.dto.NotificationSettingResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserNotificationSetting;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserNotificationSettingRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    @InjectMocks
    private NotificationSettingService notificationSettingService;

    @Captor
    private ArgumentCaptor<UserNotificationSetting> settingCaptor;

    private static final Long USER_ID = 1L;

    /* ===================== 공통 픽스처 ===================== */

    private User user() {
        return User.builder()
                .id(USER_ID)
                .email("test@fitme.com")
                .name("홍길동")
                .build();
    }

    /** 세 토글이 모두 켜져 있는 기존 설정 (부분수정 시 유지 여부를 확인하기 위해 기본값과 다르게 둔다) */
    private UserNotificationSetting existingSetting(User user) {
        return UserNotificationSetting.builder()
                .id(100L)
                .user(user)
                .notificationEmail("old@fitme.com")
                .pushEnabled(true)
                .recommendedEnabled(true)
                .reminderEnabled(true)
                .build();
    }

    private NotificationSettingRequestDto.UpdateNotificationSettingRequest request(
            String notificationEmail,
            Boolean pushEnabled,
            Boolean recommendedEnabled,
            Boolean reminderEnabled
    ) {
        return new NotificationSettingRequestDto.UpdateNotificationSettingRequest(
                notificationEmail, pushEnabled, recommendedEnabled, reminderEnabled);
    }

    /* ===================== getMyNotificationSetting ===================== */

    @Nested
    @DisplayName("getMyNotificationSetting 은 알림 설정을 조회한다")
    class GetMyNotificationSetting {

        @Test
        @DisplayName("설정이 없으면 기본 설정(토글 전체 false, 이메일=가입 이메일)을 생성해 저장하고 반환한다")
        void 설정_없으면_기본_생성() {
            // given
            User user = user();
            given(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).willReturn(Optional.of(user));
            given(userNotificationSettingRepository.findByUser(user)).willReturn(Optional.empty());
            given(userNotificationSettingRepository.save(any(UserNotificationSetting.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            NotificationSettingResponseDto.NotificationSettingResponse response =
                    notificationSettingService.getMyNotificationSetting(USER_ID);

            // then: 응답은 기본값
            assertThat(response.notificationEmail()).isEqualTo("test@fitme.com");
            assertThat(response.pushEnabled()).isFalse();
            assertThat(response.recommendedEnabled()).isFalse();
            assertThat(response.reminderEnabled()).isFalse();

            // then: 실제로 저장된 엔티티도 동일한 기본값이고 user 가 연결되어 있다
            verify(userNotificationSettingRepository).save(settingCaptor.capture());
            UserNotificationSetting saved = settingCaptor.getValue();
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(saved.getNotificationEmail()).isEqualTo("test@fitme.com");
            assertThat(saved.getPushEnabled()).isFalse();
            assertThat(saved.getRecommendedEnabled()).isFalse();
            assertThat(saved.getReminderEnabled()).isFalse();
        }

        @Test
        @DisplayName("설정이 있으면 저장 없이 기존 설정을 그대로 반환한다")
        void 설정_있으면_그대로_반환() {
            // given
            User user = user();
            given(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).willReturn(Optional.of(user));
            given(userNotificationSettingRepository.findByUser(user))
                    .willReturn(Optional.of(existingSetting(user)));

            // when
            NotificationSettingResponseDto.NotificationSettingResponse response =
                    notificationSettingService.getMyNotificationSetting(USER_ID);

            // then
            assertThat(response.notificationEmail()).isEqualTo("old@fitme.com");
            assertThat(response.pushEnabled()).isTrue();
            assertThat(response.recommendedEnabled()).isTrue();
            assertThat(response.reminderEnabled()).isTrue();

            // then: 조회만 했으므로 새로 저장하지 않는다
            verify(userNotificationSettingRepository, never()).save(any());
        }

        @Test
        @DisplayName("유저가 없거나 탈퇴한 경우(조회 쿼리가 필터) USER_NOT_FOUND ProjectException 을 던진다")
        void 유저_없음_또는_탈퇴() {
            // given: findByIdAndDeletedAtIsNull 이 탈퇴/미존재를 모두 empty 로 걸러낸다
            given(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationSettingService.getMyNotificationSetting(USER_ID))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);

            // then: 유저 조회 실패 시 설정에는 접근하지 않는다
            verify(userNotificationSettingRepository, never()).save(any());
        }
    }

    /* ===================== updateMyNotificationSetting ===================== */

    @Nested
    @DisplayName("updateMyNotificationSetting 은 값이 온 필드만 반영한다")
    class UpdateMyNotificationSetting {

        @Test
        @DisplayName("notificationEmail 만 보내면 이메일만 바뀌고 토글 세 개는 유지된다")
        void 이메일만_수정() {
            // given
            User user = user();
            UserNotificationSetting setting = existingSetting(user);
            given(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).willReturn(Optional.of(user));
            given(userNotificationSettingRepository.findByUser(user)).willReturn(Optional.of(setting));

            // when
            NotificationSettingResponseDto.NotificationSettingResponse response =
                    notificationSettingService.updateMyNotificationSetting(
                            USER_ID, request("new@fitme.com", null, null, null));

            // then: 이메일만 변경
            assertThat(response.notificationEmail()).isEqualTo("new@fitme.com");
            assertThat(response.pushEnabled()).isTrue();
            assertThat(response.recommendedEnabled()).isTrue();
            assertThat(response.reminderEnabled()).isTrue();

            // then: 영속 엔티티에도 동일하게 반영 (더티 체킹 대상)
            assertThat(setting.getNotificationEmail()).isEqualTo("new@fitme.com");
            assertThat(setting.getPushEnabled()).isTrue();
        }

        @Test
        @DisplayName("pushEnabled=false 만 보내면 해당 토글만 꺼지고 이메일과 나머지 토글은 유지된다")
        void 토글_하나만_수정() {
            // given
            User user = user();
            UserNotificationSetting setting = existingSetting(user);
            given(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).willReturn(Optional.of(user));
            given(userNotificationSettingRepository.findByUser(user)).willReturn(Optional.of(setting));

            // when: false 는 "안 보낸 것"이 아니라 "끄라는 값"이므로 반영되어야 한다
            NotificationSettingResponseDto.NotificationSettingResponse response =
                    notificationSettingService.updateMyNotificationSetting(
                            USER_ID, request(null, false, null, null));

            // then
            assertThat(response.pushEnabled()).isFalse();
            assertThat(response.notificationEmail()).isEqualTo("old@fitme.com");
            assertThat(response.recommendedEnabled()).isTrue();
            assertThat(response.reminderEnabled()).isTrue();
        }

        @Test
        @DisplayName("수정할 필드가 하나도 없으면(전 필드 null) NOTIFICATION_UPDATE_EMPTY ProjectException 을 던진다")
        void 빈_요청() {
            // given: 빈 요청 검증이 유저 조회보다 먼저라 스텁이 필요 없다 (strict stubbing)

            // when & then
            assertThatThrownBy(() -> notificationSettingService.updateMyNotificationSetting(
                    USER_ID, request(null, null, null, null)))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(UserErrorCode.NOTIFICATION_UPDATE_EMPTY);

            // then: 유저 조회에 도달하지 않는다
            verify(userRepository, never()).findByIdAndDeletedAtIsNull(any());
        }

        @Test
        @DisplayName("유저가 없거나 탈퇴한 경우 USER_NOT_FOUND ProjectException 을 던진다")
        void 유저_없음_또는_탈퇴() {
            // given
            given(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationSettingService.updateMyNotificationSetting(
                    USER_ID, request("new@fitme.com", null, null, null)))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }
    }
}