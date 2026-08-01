package umc.fitme.domain.user.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SavedPostResponseDto {

    @Builder
    public record SavedPostListResponse(
            List<SavedPostItem> savedPosts,
            PageInfo pageInfo
    ) {
    }

    @Builder
    public record SavedPostItem(
            Long savedId,
            Long postId,
            String type,
            String title,
            String organization,
            String thumbnailUrl,
            Boolean saved,
            String deadlineLabel,
            LocalDate deadlineDate,
            LocalDateTime savedAt
    ) {
    }

    @Builder
    public record SavePostResponse(
            Long savedId,
            Long postId,
            Boolean saved,
            LocalDateTime savedAt
    ) {

    }

    @Builder
    public record PageInfo(
            String nextCursor,
            Integer size,
            Boolean hasNext
    ) {
    }

    @Builder
    public record DeleteSavedPostResponse(
            Long savedId,
            Long postId,
            Boolean saved
    ) {
    }
}
