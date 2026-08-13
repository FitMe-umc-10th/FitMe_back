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
import umc.fitme.domain.user.dto.SavedPostRequestDto;
import umc.fitme.domain.user.dto.SavedPostResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.enums.SavedPostCategory;
import umc.fitme.domain.user.enums.SavedPostSort;
import umc.fitme.domain.user.service.SavedPostService;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.util.JwtUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SavedPostController.class)
@AutoConfigureMockMvc(addFilters = false)
class SavedPostControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SavedPostService savedPostService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtUtil jwtUtil;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // addFilters = false라 필터 체인이 돌지 않으므로 SecurityContextHolder에 직접 인증 정보를 채운다.
    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder) {
        User user = User.builder()
                .id(USER_ID)
                .build();
        PrincipalDetails principal = new PrincipalDetails(user, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return builder;
    }

    @Test
    @DisplayName("저장 공고 목록 조회 API 성공")
    void getSavedPosts_success() throws Exception {
        SavedPostResponseDto.SavedPostItem item =
                SavedPostResponseDto.SavedPostItem.builder()
                        .savedId(1L)
                        .postId(100L)
                        .type("CONTEST")
                        .title("공모전 테스트")
                        .organization("테스트 기관")
                        .thumbnailUrl("https://example.com/thumb.jpg")
                        .saved(true)
                        .deadlineLabel("D-3")
                        .deadlineDate(LocalDate.of(2026, 7, 14))
                        .savedAt(LocalDateTime.of(2026, 7, 11, 12, 0))
                        .build();

        SavedPostResponseDto.PageInfo pageInfo =
                SavedPostResponseDto.PageInfo.builder()
                        .nextCursor("1")
                        .size(20)
                        .hasNext(false)
                        .build();

        SavedPostResponseDto.SavedPostListResponse response =
                SavedPostResponseDto.SavedPostListResponse.builder()
                        .savedPosts(List.of(item))
                        .pageInfo(pageInfo)
                        .build();

        given(savedPostService.getSavedPosts(
                USER_ID,
                SavedPostCategory.ALL,
                SavedPostSort.RECENT,
                null,
                20
        )).willReturn(response);

        mockMvc.perform(withAuth(get("/api/v1/saved-posts")
                        .param("category", "ALL")
                        .param("sort", "RECENT")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.savedPosts[0].title").value("공모전 테스트"))
                .andExpect(jsonPath("$.result.savedPosts[0].deadlineLabel").value("D-3"))
                .andExpect(jsonPath("$.result.pageInfo.size").value(20))
                .andExpect(jsonPath("$.result.pageInfo.hasNext").value(false));
    }

    @Test
    @DisplayName("sort 파라미터를 생략하면 기본값 DEADLINE이 서비스로 전달된다")
    void getSavedPosts_defaultSort_isDeadline() throws Exception {
        mockMvc.perform(withAuth(get("/api/v1/saved-posts")
                        .param("category", "ALL")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk());

        then(savedPostService).should().getSavedPosts(
                USER_ID,
                SavedPostCategory.ALL,
                SavedPostSort.DEADLINE,
                null,
                20
        );
    }

    @Test
    @DisplayName("잘못된 category 값이면 400과 INVALID_CATEGORY 에러 코드를 반환한다")
    void getSavedPosts_invalidCategory_returns400() throws Exception {
        mockMvc.perform(withAuth(get("/api/v1/saved-posts")
                        .param("category", "FOO")
                        .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("SAVED_POST400_1"));
    }

    @Test
    @DisplayName("잘못된 sort 값이면 400과 INVALID_SORT 에러 코드를 반환한다")
    void getSavedPosts_invalidSort_returns400() throws Exception {
        mockMvc.perform(withAuth(get("/api/v1/saved-posts")
                        .param("sort", "FOO")
                        .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("SAVED_POST400_2"));
    }

    @Test
    @DisplayName("저장 공고 저장 API 성공")
    void savePost_success() throws Exception {
        SavedPostRequestDto.SavePostRequest request =
                SavedPostRequestDto.SavePostRequest.builder()
                        .postId(10L)
                        .build();

        SavedPostResponseDto.SavePostResponse response =
                SavedPostResponseDto.SavePostResponse.builder()
                        .savedId(100L)
                        .postId(10L)
                        .saved(true)
                        .savedAt(LocalDateTime.of(2026, 7, 26, 12, 0))
                        .build();

        given(savedPostService.savePost(USER_ID, 10L)).willReturn(response);

        mockMvc.perform(withAuth(post("/api/v1/saved-posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.savedId").value(100))
                .andExpect(jsonPath("$.result.postId").value(10))
                .andExpect(jsonPath("$.result.saved").value(true))
                .andExpect(jsonPath("$.result.savedAt").value("2026-07-26T12:00:00"));
    }

    @Test
    @DisplayName("postId가 없으면 400")
    void savePost_badRequest_whenPostIdIsNull() throws Exception {
        String invalidRequest = "{}";

        mockMvc.perform(post("/api/v1/saved-posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("저장 공고 취소 API 성공")
    void deleteSavedPost_success() throws Exception {
        SavedPostResponseDto.DeleteSavedPostResponse response =
                SavedPostResponseDto.DeleteSavedPostResponse.builder()
                        .savedId(100L)
                        .postId(10L)
                        .saved(false)
                        .build();

        given(savedPostService.deleteSavedPost(USER_ID, 100L)).willReturn(response);

        mockMvc.perform(withAuth(delete("/api/v1/saved-posts/{savedId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.savedId").value(100))
                .andExpect(jsonPath("$.result.postId").value(10))
                .andExpect(jsonPath("$.result.saved").value(false));
    }
}