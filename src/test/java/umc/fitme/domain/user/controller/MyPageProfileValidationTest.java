package umc.fitme.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import umc.fitme.domain.user.dto.MyPageProfileRequestDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.service.MyPageProfileService;
import umc.fitme.domain.user.service.MyPageService;
import umc.fitme.domain.user.service.NotificationSettingService;
import umc.fitme.domain.user.service.ProfileImageService;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.util.JwtUtil;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("프로필 수정 요청 검증 실패 시 400 과 명세 문구가 응답에 실린다")
class MyPageProfileValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MyPageProfileService myPageProfileService;

    @MockitoBean
    private MyPageService myPageService;

    @MockitoBean
    private NotificationSettingService notificationSettingService;

    @MockitoBean
    private ProfileImageService profileImageService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtUtil jwtUtil;

    private static final Long USER_ID = 1L;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder) {
        User user = User.builder().id(USER_ID).build();
        PrincipalDetails principal = new PrincipalDetails(user, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return builder;
    }

    @Test
    @DisplayName("gpa 가 상한(4.50)을 넘으면 400 과 '유효하지 않은 학점입니다.' 를 내려주고 서비스는 호출되지 않는다")
    void gpa_범위_초과() throws Exception {
        MyPageProfileRequestDto.UpdateProfileRequest request =
                new MyPageProfileRequestDto.UpdateProfileRequest(
                        new BigDecimal("4.60"), null, null, null, null);

        mockMvc.perform(withAuth(patch("/api/v1/users/me/profile"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result.gpa").value("유효하지 않은 학점입니다."));

        verify(myPageProfileService, never()).updateProfile(anyLong(), any());
    }

    @Test
    @DisplayName("gpa 소수점 자릿수가 초과하면 400 과 '유효하지 않은 학점입니다.' 를 내려준다")
    void gpa_자릿수_초과() throws Exception {
        MyPageProfileRequestDto.UpdateProfileRequest request =
                new MyPageProfileRequestDto.UpdateProfileRequest(
                        new BigDecimal("3.456"), null, null, null, null);

        mockMvc.perform(withAuth(patch("/api/v1/users/me/profile"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.gpa").value("유효하지 않은 학점입니다."));
    }

    @Test
    @DisplayName("incomeBracket 이 범위를 벗어나면 400 과 '유효하지 않은 소득구간입니다.' 를 내려준다")
    void 소득구간_범위_초과() throws Exception {
        MyPageProfileRequestDto.UpdateProfileRequest request =
                new MyPageProfileRequestDto.UpdateProfileRequest(
                        null, 11, null, null, null);

        mockMvc.perform(withAuth(patch("/api/v1/users/me/profile"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.incomeBracket").value("유효하지 않은 소득구간입니다."));

        verify(myPageProfileService, never()).updateProfile(anyLong(), any());
    }

    @Test
    @DisplayName("gpa 와 incomeBracket 이 함께 잘못되면 두 필드 메시지가 모두 실린다")
    void 두_필드_동시_검증_실패() throws Exception {
        MyPageProfileRequestDto.UpdateProfileRequest request =
                new MyPageProfileRequestDto.UpdateProfileRequest(
                        new BigDecimal("-1.00"), 0, null, null, null);

        mockMvc.perform(withAuth(patch("/api/v1/users/me/profile"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.gpa").value("유효하지 않은 학점입니다."))
                .andExpect(jsonPath("$.result.incomeBracket").value("유효하지 않은 소득구간입니다."));
    }
}