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
import umc.fitme.domain.user.dto.MyPageProfileRequestDto;
import umc.fitme.domain.user.service.MyPageProfileService;
import umc.fitme.domain.user.service.MyPageService;
import umc.fitme.global.config.SecurityConfig;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.exception.CustomAccessDenied;
import umc.fitme.global.security.exception.CustomEntryPoint;
import umc.fitme.global.security.handler.OAuth2FailureHandler;
import umc.fitme.global.security.handler.OAuth2SuccessHandler;
import umc.fitme.global.security.service.CustomOAuth2UserService;
import umc.fitme.global.security.util.JwtUtil;

import java.math.BigDecimal;

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
        PrincipalDetails principal = org.mockito.Mockito.mock(PrincipalDetails.class);
        BDDMockito.given(principal.getUser().getId()).willReturn(1L);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    @Test
    @DisplayName("gpa 가 상한(4.50)을 넘으면 400 과 '유효하지 않은 학점입니다.' 를 내려주고 서비스는 호출되지 않는다")
    void gpa_범위_초과() throws Exception {
        MyPageProfileRequestDto.UpdateProfileRequest request =
                new MyPageProfileRequestDto.UpdateProfileRequest(
                        new BigDecimal("4.60"), null, null, null, null);

        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(loginUser())
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

        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(loginUser())
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

        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(loginUser())
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

        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(loginUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.gpa").value("유효하지 않은 학점입니다."))
                .andExpect(jsonPath("$.result.incomeBracket").value("유효하지 않은 소득구간입니다."));
    }
}