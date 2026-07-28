package umc.fitme.domain.onboarding.controller;

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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import umc.fitme.domain.onboarding.dto.OnboardingRequestDto;
import umc.fitme.domain.onboarding.dto.OnboardingResponseDto;
import umc.fitme.domain.onboarding.service.OnboardingService;
import umc.fitme.global.config.SecurityConfig;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.exception.CustomAccessDenied;
import umc.fitme.global.security.exception.CustomEntryPoint;
import umc.fitme.global.security.handler.OAuth2FailureHandler;
import umc.fitme.global.security.handler.OAuth2SuccessHandler;
import umc.fitme.global.security.service.CustomOAuth2UserService;
import umc.fitme.global.security.util.JwtUtil;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OnboardingController.class)
@Import({SecurityConfig.class, CustomEntryPoint.class, CustomAccessDenied.class})
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OnboardingService onboardingService;

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

    @Test
    @DisplayName("온보딩 완료 요청 성공")
    void complete_success() throws Exception {
        OnboardingRequestDto request = new OnboardingRequestDto(
                "서울",
                "홍익대학교",
                4.1f,
                "3구간",
                java.util.List.of("개발", "AI"),
                java.util.List.of("백엔드")
        );

        PrincipalDetails principal = org.mockito.Mockito.mock(PrincipalDetails.class);
        BDDMockito.given(principal.getUser().getId()).willReturn(1L);
        BDDMockito.given(onboardingService.complete(1L, request))
                .willReturn(OnboardingResponseDto.of(true));

        mockMvc.perform(post("/api/v1/onboarding")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,
                                        AuthorityUtils.createAuthorityList("ROLE_USER")
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 온보딩 완료 요청 실패")
    @WithAnonymousUser
    void complete_unauthorized() throws Exception {
        OnboardingRequestDto request = new OnboardingRequestDto(
                "서울",
                "홍익대학교",
                4.1f,
                "3구간",
                java.util.List.of("개발"),
                java.util.List.of("백엔드")
        );

        mockMvc.perform(post("/api/v1/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
