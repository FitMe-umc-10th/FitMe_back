package umc.fitme.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PostResponseDTO {


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularPostDTO {
        private Long postId;
        private String type;
        private String title;
        private Integer deadlineLabel;
        private String thumbnailUrl;
        private String organizer;
        private Boolean saved;
        private Boolean active;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularPostListDTO {
        private List<PopularPostDTO> popularPosts;
        private Boolean hasNext;
        private Long nextIdCursor;
        private String nextDeadlineCursor;
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostPreviewListDTO {
        private String name;
        private Boolean hasNext;
        private Long nextCursor;
        private List<PostPreviewDTO> posts;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostPreviewDTO {
        private Long postId;
        private String type;
        private String title;
        private Integer deadlineLabel;
        private String thumbnailUrl;
        private String organizer;
        private Boolean saved;
        private Boolean active;
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDetailDTO {
        private Long postId;
        private String type;
        private String title;
        private String organizer;
        private String thumbnailUrl;
        private Integer viewCount;
        private Integer savedCount;
        private Boolean saved;
        private Boolean active;
        private String summary;
        private String applyStartDate;
        private String applyEndDate;
        private Integer deadlineLabel;
        private String applicationMethod;
        private String applicationUrl;


        // 공고 종류에 해당하는 쪽만 채워지고 나머지는 null로 나간다.
        private ScholarshipDetailDTO scholarshipDetail;
        private ContestDetailDTO contestDetail;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScholarshipDetailDTO {
        private String supportAmount;
        private String gradeRequirement;
        private String incomeRequirement;
        private String regionRequirement;
        private String universityRequirement;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContestDetailDTO {
        private String posterImageUrl;
        private String target;
        private String participantLimit;
        private String rewardTotal;
    }
}
