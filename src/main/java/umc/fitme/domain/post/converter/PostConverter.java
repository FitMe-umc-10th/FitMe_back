package umc.fitme.domain.post.converter;

import umc.fitme.domain.post.dto.publicapi.PublicApiScholarshipDTO;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.PostType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class PostConverter {


    private static Integer calculateDDay(LocalDate applyEndAt) {
        if (applyEndAt == null) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), applyEndAt);
    }


    private static String extractThumbnailUrl(Post post) {
        if (post instanceof Contest) {
            return ((Contest) post).getPosterImageUrl();
        }
        return "https://default-image-url.com/scholarship.png";
    }

    public static PostResponseDTO.PopularPostDTO toPopularPostDTO(Post post) {
        return PostResponseDTO.PopularPostDTO.builder()
                .postId(post.getId())
                .type(post.getPostType().name())
                .title(post.getTitle())
                .deadlineLabel(calculateDDay(post.getApplyEndAt()))
                .thumbnailUrl(extractThumbnailUrl(post))
                .organization(post.getOrganizer())
                .saved(false)
                .build();
    }

    public static PostResponseDTO.PopularPostListDTO toPopularPostListDTO(List<Post> postList, boolean hasNext, Long nextCursor) {
        List<PostResponseDTO.PopularPostDTO> postDTOs = postList.stream()
                .map(PostConverter::toPopularPostDTO)
                .collect(Collectors.toList());

        return PostResponseDTO.PopularPostListDTO.builder()
                .hasNext(hasNext)
                .nextIdCursor(nextCursor)
                .nextDeadlineCursor(null)
                .popularPosts(postDTOs)
                .build();
    }


    public static PostResponseDTO.PostPreviewDTO toPostPreviewDTO(Post post, boolean isSaved) {
        return PostResponseDTO.PostPreviewDTO.builder()
                .postId(post.getId())
                .type(post.getPostType().name())
                .title(post.getTitle())
                .deadlineLabel(calculateDDay(post.getApplyEndAt()))
                .thumbnailUrl(extractThumbnailUrl(post))
                .organization(post.getOrganizer())
                .saved(isSaved)
                .build();
    }


    public static PostResponseDTO.PostPreviewListDTO toPostPreviewListDTO(List<Post> postList, boolean hasNext, Long nextCursor, List<Long> savedPostIds) {
        List<PostResponseDTO.PostPreviewDTO> postDTOs = postList.stream()
                .map(post -> {
                    boolean isSaved = savedPostIds != null && savedPostIds.contains(post.getId());
                    return toPostPreviewDTO(post, isSaved);
                })
                .collect(Collectors.toList());

        return PostResponseDTO.PostPreviewListDTO.builder()
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .posts(postDTOs)
                .build();
    }


    public static PostResponseDTO.PostDetailDTO toPostDetailDTO(Post post) {


        PostResponseDTO.ScholarshipDetailDTO scholarshipDetailDTO = null;


        if (post instanceof Scholarship scholarship) {
            scholarshipDetailDTO = PostResponseDTO.ScholarshipDetailDTO.builder()
                    .supportAmount(scholarship.getSupportAmount())
                    .gradeRequirement(scholarship.getGradeRequirement())
                    .incomeRequirement(scholarship.getIncomeRequirement())
                    .regionRequirement(scholarship.getRegionRequirement())
                    .build();
        }


        return PostResponseDTO.PostDetailDTO.builder()
                .postId(post.getId())
                .type(post.getPostType().name())
                .title(post.getTitle())
                .organization(post.getOrganizer())
                .viewCount(post.getViewCount())
                .savedCount(post.getSavedCount())
                .saved(false)
                .summary(post.getSummary())
                .applyEndDate(post.getApplyEndAt() != null ? post.getApplyEndAt().toString() : null)
                .deadlineLabel(calculateDDay(post.getApplyEndAt()))
                .applicationMethod(post.getApplicationMethod())
                .scholarshipDetail(scholarshipDetailDTO)
                .build();
    }


    public static Scholarship toScholarshipEntity(PublicApiScholarshipDTO dto) {

        LocalDate parsedStartDate = parseDate(dto.getStartDate());
        LocalDate parsedDeadlineDate = parseDate(dto.getDeadlineDate());

        return Scholarship.builder()
                .postType(PostType.SCHOLARSHIP)
                .title(dto.getTitle())
                .organizer(dto.getOrganizer())
                .applyStartAt(parsedStartDate)
                .applyEndAt(parsedDeadlineDate)
                .summary(dto.getSummary())
                .applicationMethod("홈페이지 지원")
                .applicationUrl(dto.getApplicationUrl())


                .gradeRequirement(dto.getGradeRequirement() != null ? dto.getGradeRequirement() : "기준 없음")
                .incomeRequirement(dto.getIncomeRequirement() != null ? dto.getIncomeRequirement() : "기준 없음")
                .regionRequirement(dto.getRegionRequirement() != null ? dto.getRegionRequirement() : "제한 없음")
                .supportAmount(dto.getSummary() != null ? dto.getSummary() : "상세 참조")
                .build();
    }


    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return LocalDate.of(2099, 12, 31);
        }
        try {
            return LocalDate.parse(dateString);
        } catch (Exception e) {
            return LocalDate.of(2099, 12, 31);
        }
    }
}