package umc.fitme.domain.user.dto;

import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import org.hibernate.Hibernate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UserApplicationResponseDto {

    /* POST 자동등록 */
    public record CreateResponse(
            Long userApplicationId,
            Long postId,
            String status,
            Boolean isApplied,
            String applicationUrl
    ) {
        public static CreateResponse from(UserApplication userApplication) {
            return new CreateResponse(
                    userApplication.getId(),
                    userApplication.getPost().getId(),
                    userApplication.getStatus().name(),
                    userApplication.getIsApplied(),
                    userApplication.getPost().getApplicationUrl()
            );
        }
    }

    /* GET 목록 */
    public record ListResponse(
            List<SummaryResponse> userApplications
    ) {
        public static ListResponse from(List<UserApplication> userApplications) {
            return new ListResponse(
                    userApplications.stream()
                            .map(SummaryResponse::from)
                            .toList()
            );
        }
    }

    public record SummaryResponse(
            Long userApplicationId,
            Long postId,
            String postType,
            String title,
            String organizer,
            String posterImageUrl,
            String status,
            Boolean isApplied,
            LocalDateTime updatedAt,
            String memo
    ) {
        public static SummaryResponse from(UserApplication userApplication) {
            Post post = userApplication.getPost();

            String posterImageUrl = null;
            if (post instanceof Contest contest) {
                posterImageUrl = contest.getPosterImageUrl();
            }

            return new SummaryResponse(
                    userApplication.getId(),
                    post.getId(),
                    post.getPostType().name(),
                    post.getTitle(),
                    post.getOrganizer(),
                    posterImageUrl,
                    userApplication.getStatus().name(),
                    userApplication.getIsApplied(),
                    userApplication.getUpdatedAt(),
                    userApplication.getMemo()
            );
        }
    }

    /* GET 상세 */
    public record DetailResponse(
            Long userApplicationId,
            String status,
            Boolean isApplied,
            String memo,
            PostDetailResponse post
    ) {
        public static DetailResponse from(UserApplication userApplication) {
            return new DetailResponse(
                    userApplication.getId(),
                    userApplication.getStatus().name(),
                    userApplication.getIsApplied(),
                    userApplication.getMemo(),
                    PostDetailResponse.from(userApplication.getPost())
            );
        }
    }

    public record PostDetailResponse(
            Long postId,
            String postType,
            String title,
            String organizer,
            LocalDate applyStartAt,
            LocalDate applyEndAt,
            String summary,
            String applicationMethod,
            String applicationUrl,
            Integer viewCount,
            Integer savedCount,
            ScholarshipResponse scholarship,
            ContestResponse contest
    ) {
        public static PostDetailResponse from(Post post) {
            Post unproxiedPost = Hibernate.unproxy(post, Post.class);

            ScholarshipResponse scholarship = null;
            ContestResponse contest = null;

            if (unproxiedPost instanceof Scholarship scholarshipPost) {
                scholarship = ScholarshipResponse.from(scholarshipPost);
            }

            if (unproxiedPost instanceof Contest contestPost) {
                contest = ContestResponse.from(contestPost);
            }

            return new PostDetailResponse(
                    unproxiedPost.getId(),
                    unproxiedPost.getPostType().name(),
                    unproxiedPost.getTitle(),
                    unproxiedPost.getOrganizer(),
                    unproxiedPost.getApplyStartAt(),
                    unproxiedPost.getApplyEndAt(),
                    unproxiedPost.getSummary(),
                    unproxiedPost.getApplicationMethod(),
                    unproxiedPost.getApplicationUrl(),
                    unproxiedPost.getViewCount(),
                    unproxiedPost.getSavedCount(),
                    scholarship,
                    contest
            );
        }
    }

    public record ScholarshipResponse(
            String gradeRequirement,
            String incomeRequirement,
            String regionRequirement,
            String supportAmount
    ) {
        public static ScholarshipResponse from(Scholarship scholarship) {
            return new ScholarshipResponse(
                    scholarship.getGradeRequirement(),
                    scholarship.getIncomeRequirement(),
                    scholarship.getRegionRequirement(),
                    scholarship.getSupportAmount()
            );
        }
    }

    public record ContestResponse(
            String posterImageUrl,
            String target,
            String participantLimit,
            String rewardTotal
    ) {
        public static ContestResponse from(Contest contest) {
            return new ContestResponse(
                    contest.getPosterImageUrl(),
                    contest.getTarget(),
                    contest.getParticipantLimit(),
                    contest.getRewardTotal()
            );
        }
    }

    /* PATCH status */
    public record UpdateStatusResponse(
            Long userApplicationId,
            String status,
            Boolean isApplied,
            LocalDateTime updatedAt
    ) {
        public static UpdateStatusResponse from(UserApplication userApplication) {
            return new UpdateStatusResponse(
                    userApplication.getId(),
                    userApplication.getStatus().name(),
                    userApplication.getIsApplied(),
                    userApplication.getUpdatedAt()
            );
        }
    }

    /* PATCH memo */
    public record UpdateMemoResponse(
            Long userApplicationId,
            String memo,
            LocalDateTime updatedAt
    ) {
        public static UpdateMemoResponse from(UserApplication userApplication) {
            return new UpdateMemoResponse(
                    userApplication.getId(),
                    userApplication.getMemo(),
                    userApplication.getUpdatedAt()
            );
        }
    }

    /* DELETE */
    public record DeleteResponse(
            Long userApplicationId
    ) {
    }
}