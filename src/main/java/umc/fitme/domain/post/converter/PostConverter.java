package umc.fitme.domain.post.converter;

import umc.fitme.domain.post.dto.publicapi.PublicApiScholarshipDTO;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.PostType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
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

    public static PostResponseDTO.PopularPostDTO toPopularPostDTO(Post post, boolean isSaved) {
        return PostResponseDTO.PopularPostDTO.builder()
                .postId(post.getId())
                .type(post.getPostType().name())
                .title(post.getTitle())
                .deadlineLabel(calculateDDay(post.getApplyEndAt()))
                .thumbnailUrl(extractThumbnailUrl(post))
                .organizer(post.getOrganizer())
                .saved(isSaved)
                .build();
    }

    public static PostResponseDTO.PopularPostListDTO toPopularPostListDTO(
            List<Post> postList,
            boolean hasNext,
            Long nextCursor,
            Collection<Long> savedPostIds
    ) {
        List<PostResponseDTO.PopularPostDTO> postDTOs = postList.stream()
                .map(post -> {
                    boolean isSaved = savedPostIds != null && savedPostIds.contains(post.getId());
                    return toPopularPostDTO(post, isSaved);
                })
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
                .organizer(post.getOrganizer())
                .saved(isSaved)
                .build();
    }


    public static PostResponseDTO.PostPreviewListDTO toPostPreviewListDTO(
            List<Post> postList,
            boolean hasNext,
            Long nextCursor,
            Collection<Long> savedPostIds
    ) {
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
        return toPostDetailDTO(post, false);
    }

    /**
     * 상세 공고 응답에 사용자별 찜 여부와 장학금 전용 조건을 함께 조립한다.
     */
    public static PostResponseDTO.PostDetailDTO toPostDetailDTO(Post post, boolean isSaved) {


        PostResponseDTO.ScholarshipDetailDTO scholarshipDetailDTO = null;
        PostResponseDTO.ContestDetailDTO contestDetailDTO = null;


        if (post instanceof Scholarship scholarship) {
            scholarshipDetailDTO = PostResponseDTO.ScholarshipDetailDTO.builder()
                    .supportAmount(scholarship.getSupportAmount())
                    .gradeRequirement(scholarship.getGradeRequirement())
                    .incomeRequirement(scholarship.getIncomeRequirement())
                    .regionRequirement(scholarship.getRegionRequirement())
                    .universityRequirement(scholarship.getUniversityRequirement())
                    .build();
        } else if (post instanceof Contest contest) {
            contestDetailDTO = PostResponseDTO.ContestDetailDTO.builder()
                    .posterImageUrl(contest.getPosterImageUrl())
                    .target(contest.getTarget())
                    .participantLimit(contest.getParticipantLimit())
                    .rewardTotal(contest.getRewardTotal())
                    .build();
        }


        return PostResponseDTO.PostDetailDTO.builder()
                .postId(post.getId())
                .type(post.getPostType().name())
                .title(post.getTitle())
                .organizer(post.getOrganizer())
                .thumbnailUrl(extractThumbnailUrl(post))
                .viewCount(post.getViewCount())
                .savedCount(post.getSavedCount())
                .saved(isSaved)
                .summary(post.getSummary())
                .applyStartDate(post.getApplyStartAt() != null ? post.getApplyStartAt().toString() : null)
                .applyEndDate(post.getApplyEndAt() != null ? post.getApplyEndAt().toString() : null)
                .deadlineLabel(calculateDDay(post.getApplyEndAt()))
                .applicationMethod(post.getApplicationMethod())
                .applicationUrl(post.getApplicationUrl())
                .scholarshipDetail(scholarshipDetailDTO)
                .contestDetail(contestDetailDTO)
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
                .universityRequirement(dto.getUniversityRequirement() != null
                        ? dto.getUniversityRequirement()
                        : "제한 없음")
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
