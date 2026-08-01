package umc.fitme.domain.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import umc.fitme.domain.user.service.SavedPostService;
import umc.fitme.global.config.SecurityConfig;
import umc.fitme.global.security.exception.CustomAccessDenied;
import umc.fitme.global.security.exception.CustomEntryPoint;
import umc.fitme.global.security.handler.OAuth2FailureHandler;
import umc.fitme.global.security.handler.OAuth2SuccessHandler;
import umc.fitme.global.security.service.CustomOAuth2UserService;
import umc.fitme.global.security.util.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SavedPostControllerTest는 addFilters=false로 SecurityFilterChain을 우회하므로,
 * 인증이 실제로 강제되는지는 이 테스트에서 진짜 SecurityConfig를 로드해 검증한다.
 */
@WebMvcTest(SavedPostController.class)
@Import({SecurityConfig.class, CustomEntryPoint.class, CustomAccessDenied.class})
class SavedPostSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavedPostService savedPostService;

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
    @DisplayName("토큰 없이 GET /api/v1/saved-posts 요청하면 실제 SecurityFilterChain에 의해 401이 반환된다")
    void getSavedPosts_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/saved-posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("토큰 없이 POST /api/v1/saved-posts 요청하면 실제 SecurityFilterChain에 의해 401이 반환된다")
    void savePost_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/saved-posts")
                        .contentType("application/json")
                        .content("{\"postId\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("토큰 없이 DELETE /api/v1/saved-posts/{savedId} 요청하면 실제 SecurityFilterChain에 의해 401이 반환된다")
    void deleteSavedPost_withoutToken_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/saved-posts/{savedId}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }
}
