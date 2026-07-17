package umc.fitme.domain.user.dto;

import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;

public class MyPageResponseDto {

    /* GET 마이페이지 전체 응답*/
    public record MyPageResponse(
            ProfileResponse profile,
            ActivitySummaryResponse activitySummary
    ) {
        public static MyPageResponse of(
                User user,
                UserDetail userDetail,
                long completedApplicationCount,
                long totalScholarshipAmount,
                long pendingResultCount
        ) {
            return new MyPageResponse(
                    ProfileResponse.from(user, userDetail),
                    ActivitySummaryResponse.of(
                            completedApplicationCount,
                            totalScholarshipAmount,
                            pendingResultCount
                    )
            );
        }
    }

    /* 프로필 영역 */
    public record ProfileResponse(
            String name,
            String universityName,
            String profileImageUrl
    ) {
        public static ProfileResponse from(User user, UserDetail userDetail) {
            return new ProfileResponse(
                    user.getName(),
                    userDetail.getUniversityName(),
                    userDetail.getProfileImageUrl()
            );
        }
    }

    /* 활동 요약 영역 */
    public record ActivitySummaryResponse(
            long completedApplicationCount,
            long totalScholarshipAmount,
            long pendingResultCount
    ) {
        public static ActivitySummaryResponse of(
                long completedApplicationCount,
                long totalScholarshipAmount,
                long pendingResultCount
        ) {
            return new ActivitySummaryResponse(
                    completedApplicationCount,
                    totalScholarshipAmount,
                    pendingResultCount
            );
        }
    }
}