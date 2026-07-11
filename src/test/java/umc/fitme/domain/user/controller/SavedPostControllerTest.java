package umc.fitme.domain.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import umc.fitme.domain.user.dto.SavedPostResponseDto;
import umc.fitme.domain.user.enums.SavedPostCategory;
import umc.fitme.domain.user.enums.SavedPostSort;
import umc.fitme.domain.user.service.SavedPostService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavedPostController.class)
@AutoConfigureMockMvc(addFilters = false)
class SavedPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavedPostService savedPostService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

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
                SavedPostCategory.ALL,
                SavedPostSort.RECENT,
                null,
                20
        )).willReturn(response);

        mockMvc.perform(get("/api/v1/saved-posts")
                        .param("category", "ALL")
                        .param("sort", "RECENT")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.savedPosts[0].title").value("공모전 테스트"))
                .andExpect(jsonPath("$.result.savedPosts[0].deadlineLabel").value("D-3"))
                .andExpect(jsonPath("$.result.pageInfo.size").value(20))
                .andExpect(jsonPath("$.result.pageInfo.hasNext").value(false));
    }
}