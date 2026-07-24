package umc.fitme.domain.notify.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class AnnouncementResponseDto {

    @Builder
    public record AnnouncementListResponse(
            List<AnnouncementItem> announcements
    ) {
    }

    @Builder
    public record AnnouncementItem(
            Long announcementId,
            String category,
            String categoryName,
            String title,
            LocalDateTime createdAt,
            String createdAtString,
            Boolean isNew
    ) {
    }

    @Builder
    public record AnnouncementDetailResponse(
            Long announcementId,
            String title,
            String content,
            LocalDateTime createdAt
    ) {
    }
}