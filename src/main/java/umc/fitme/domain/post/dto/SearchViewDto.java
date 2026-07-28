package umc.fitme.domain.post.dto;

import lombok.Builder;
import umc.fitme.domain.post.enums.FluctuationType;
import umc.fitme.domain.post.enums.PostType;

import java.time.LocalDateTime;
import java.util.List;

public class SearchViewDto {

    @Builder
    public record SearchViewRes(
       List<RecentKeywordDto> recentKeywords,
        RealtimePostGroupDto realtimePosts
    ){}

    @Builder
    public record RecentKeywordDto(
         Long searchId,
         String keyword
    ){}

    @Builder
    public record RealtimePostGroupDto(
            LocalDateTime baseTime,
            List<RealtimePostDto> posts
    ){}

    @Builder
    public record RealtimePostDto(
            Integer rank,
            Long postId,
            PostType type,
            String title,
            FluctuationType fluctuation
    ){}
}
