package umc.fitme.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PostResponseDTO {

    // 1. 인기 공고 단건 DTO
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
        private String organization;
        private Boolean saved;
    }

    // 2. 인기 공고 리스트 및 페이징 정보 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularPostListDTO {
        private List<PopularPostDTO> popularPosts; // Converter에서 생성한 리스트를 담을 변수
        private Boolean hasNext;
        private Long nextIdCursor;
        private String nextDeadlineCursor;
    }

    //공고 상세 조회

    // 3. 공고 상세 조회 응답 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDetailDTO {
        private Long postId;
        private String type;
        private String title;
        private String organization;
        private Integer viewCount;
        private Integer savedCount;
        private Boolean saved;
        private String summary;

        // 날짜 포맷 (예: "2026-05-31T18:00:00")
        private String applyEndDate;
        private Integer deadlineLabel;
        private String applicationMethod;

        // 장학금 전용 상세 정보 (Type이 CONTEST일 경우 이 필드는 null)
        private ScholarshipDetailDTO scholarshipDetail;
    }

    // 4. 장학금 특화 정보 객체 (Nested Object)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScholarshipDetailDTO {
        private String supportAmount;    // 지원금액 및 지원내역
        private String gradeRequirement; // 성적기준
        private String incomeRequirement;// 소득기준
        private String regionRequirement;// 지역거주여부 등 특정 자격
    }
}