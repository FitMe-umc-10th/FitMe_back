package umc.fitme.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import umc.fitme.domain.user.dto.NotificationSettingRequestDto;
import umc.fitme.domain.user.service.MyPageProfileService;
import umc.fitme.domain.user.service.MyPageService;
import umc.fitme.domain.user.service.NotificationSettingService;
import umc.fitme.domain.user.service.ProfileImageService;
import umc.fitme.global.config.SecurityConfig;
import umc.fitme.global.security.entity.CustomUserDetails;
import umc.fitme.global.security.exception.CustomAccessDenied;
import umc.fitme.global.security.exception.CustomEntryPoint;
import umc.fitme.global.security.handler.OAuth2FailureHandler;
import umc.fitme.global.security.handler.OAuth2SuccessHandler;
import umc.fitme.global.security.service.CustomOAuth2UserService;
import umc.fitme.global.security.util.JwtUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyPageController.class)
@Import({SecurityConfig.class, CustomEntryPoint.class, CustomAccessDenied.class})
@DisplayName("알림 설정 수정 요청 검증 실패 시 400 과 명세 문구가 응답에 실린다")
class NotificationSettingValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationSettingService notificationSettingService;

    @MockitoBean
    private MyPageProfileService myPageProfileService;

    @MockitoBean
    private MyPageService myPageService;

    @MockitoBean
    private ProfileImageService profileImageService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private OAuth2FailureHandler oAuth2FailureHandler;

    private RequestPostProcessor loginUser() {
        CustomUserDetails principal = org.mockito.Mockito.mock(CustomUserDetails.class);
        BDDMockito.given(principal.getUserId()).willReturn(1L);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    @Test
    @DisplayName("notificationEmail 이 빈 문자열이면 400 과 '올바르지 않은 이메일 형식입니다.' 를 내려주고 서비스는 호출되지 않는다")
    void 이메일_빈문자열() throws Exception {
        NotificationSettingRequestDto.UpdateNotificationSettingRequest request =
                new NotificationSettingRequestDto.UpdateNotificationSettingRequest("", null, null, null);

        mockMvc.perform(patch("/api/v1/users/me/notification-settings")
                        .with(loginUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result.notificationEmail").value("올바르지 않은 이메일 형식입니다."));

        verify(notificationSettingService, never()).updateMyNotificationSetting(anyLong(), any());
    }

    @Test
    @DisplayName("notificationEmail 형식이 잘못되면(abc) 400 과 '올바르지 않은 이메일 형식입니다.' 를 내려준다")
    void 이메일_형식_오류() throws Exception {
        NotificationSettingRequestDto.UpdateNotificationSettingRequest request =
                new NotificationSettingRequestDto.UpdateNotificationSettingRequest("abc", null, null, null);

        mockMvc.perform(patch("/api/v1/users/me/notification-settings")
                        .with(loginUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.notificationEmail").value("올바르지 않은 이메일 형식입니다."));

        verify(notificationSettingService, never()).updateMyNotificationSetting(anyLong(), any());
    }
}