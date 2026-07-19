package umc.fitme.domain.user.dto;

import umc.fitme.domain.interest.entity.Interest;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;

import java.util.List;

public class MyPageProfileResponseDto {

    /* 관심 분야 항목 (전체 목록 + 선택 여부) */
    public record InterestItem(
            Long interestId,
            String interestName,
            Boolean selected
    ) {
        public static InterestItem of(Interest interest, boolean selected) {
            return new InterestItem(
                    interest.getId(),
                    interest.getInterestName(),
                    selected
            );
        }
    }

    /* GET 프로필 조회 응답 */
    public record ProfileResponse(
            String name,
            String universityName,
            String profileImageUrl,
            Float gpa,
            Integer incomeBracket,
            String region,
            List<InterestItem> interests
    ) {
        public static ProfileResponse of(User user, UserDetail userDetail, List<InterestItem> interests) {
            return new ProfileResponse(
                    user.getName(),
                    userDetail.getUniversityName(),
                    userDetail.getProfileImageUrl(),
                    userDetail.getGpa(),
                    userDetail.getIncomeBracket(),
                    userDetail.getRegion(),
                    interests
            );
        }
    }

    /* PATCH 프로필 수정 응답 (interests 는 GET과 동일하게 전체 목록 + selected) */
    public record UpdateProfileResponse(
            Float gpa,
            Integer incomeBracket,
            String region,
            String profileImageUrl,
            List<InterestItem> interests
    ) {
        public static UpdateProfileResponse of(UserDetail userDetail, List<InterestItem> interests) {
            return new UpdateProfileResponse(
                    userDetail.getGpa(),
                    userDetail.getIncomeBracket(),
                    userDetail.getRegion(),
                    userDetail.getProfileImageUrl(),
                    interests
            );
        }
    }
}