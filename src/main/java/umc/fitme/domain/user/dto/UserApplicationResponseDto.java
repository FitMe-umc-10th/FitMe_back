package umc.fitme.domain.user.dto;

import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.entity.mapping.UserApplicationPostSnapshot;
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
        public static DetailResponse from(
                UserApplication userApplication,
                UserApplicationPostSnapshot snapshot
        ) {
            return new DetailResponse(
                    userApplication.getId(),
                    userApplication.getStatus().name(),
                    userApplication.getIsApplied(),
                    userApplication.getMemo(),
                    PostDetailResponse.from(snapshot)
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
        public static PostDetailResponse from(UserApplicationPostSnapshot snapshot) {
            ScholarshipResponse scholarship = null;
            ContestResponse contest = null;

            if (snapshot.getPostType() == PostType.SCHOLARSHIP) {
                scholarship = ScholarshipResponse.from(snapshot);
            }

            if (snapshot.getPostType() == PostType.CONTEST) {
                contest = ContestResponse.from(snapshot);
            }

            return new PostDetailResponse(
                    snapshot.getOriginalPostId(),
                    snapshot.getPostType().name(),
                    snapshot.getTitle(),
                    snapshot.getOrganizer(),
                    snapshot.getApplyStartAt(),
                    snapshot.getApplyEndAt(),
                    snapshot.getSummary(),
                    snapshot.getApplicationMethod(),
                    snapshot.getApplicationUrl(),
                    null,
                    null,
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
        public static ScholarshipResponse from(UserApplicationPostSnapshot snapshot) {
            return new ScholarshipResponse(
                    snapshot.getGradeRequirement(),
                    snapshot.getIncomeRequirement(),
                    snapshot.getRegionRequirement(),
                    snapshot.getSupportAmount()
            );
        }
    }

    public record ContestResponse(
            String posterImageUrl,
            String target,
            String participantLimit,
            String rewardTotal
    ) {
        public static ContestResponse from(UserApplicationPostSnapshot snapshot) {
            return new ContestResponse(
                    snapshot.getPosterImageUrl(),
                    snapshot.getTarget(),
                    snapshot.getParticipantLimit(),
                    snapshot.getRewardTotal()
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
        public static DeleteResponse from(UserApplication userApplication) {
            return new DeleteResponse(userApplication.getId());
        }
    }
}