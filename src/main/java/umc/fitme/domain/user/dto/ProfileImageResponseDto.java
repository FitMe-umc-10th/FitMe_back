package umc.fitme.domain.user.dto;

public class ProfileImageResponseDto {

    /* 프로필 이미지 presigned URL 발급 응답 */
    public record PresignedUrlResponse(
            String uploadUrl,
            String fileUrl
    ) {
        public static PresignedUrlResponse of(String uploadUrl, String fileUrl) {
            return new PresignedUrlResponse(uploadUrl, fileUrl);
        }
    }
}