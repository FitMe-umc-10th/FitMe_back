package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import umc.fitme.domain.user.dto.ProfileImageRequestDto;
import umc.fitme.domain.user.dto.ProfileImageResponseDto;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    /* 허용 확장자 -> 해당 확장자에 대응하는 contentType */
    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE =
            Map.of("jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png");
    private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(5);
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    /* 프로필 이미지 업로드용 presigned URL(uploadUrl)과 최종 조회 URL(fileUrl)을 발급 (5분 유효) */
    public ProfileImageResponseDto.PresignedUrlResponse createPresignedUrl(Long userId, ProfileImageRequestDto.PresignedUrlRequest request) {
        String extension = extractExtension(request.fileName());
        String contentType = request.contentType().toLowerCase(Locale.ROOT);

        String expectedContentType = EXTENSION_TO_CONTENT_TYPE.get(extension);
        if (expectedContentType == null || !expectedContentType.equals(contentType)) {
            throw new ProjectException(UserErrorCode.INVALID_IMAGE_TYPE);
        }

        Long fileSize = request.fileSize();
        if (fileSize == null || fileSize <= 0 || fileSize > MAX_FILE_SIZE) {
            throw new ProjectException(UserErrorCode.INVALID_IMAGE_SIZE);
        }

        String key = "profile/" + userId + "/" + UUID.randomUUID() + "." + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_DURATION)
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

        return ProfileImageResponseDto.PresignedUrlResponse.of(uploadUrl, fileUrl);
    }

    /* 파일 이름에서 마지막 '.' 뒤 확장자를 소문자로 추출 */
    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            throw new ProjectException(UserErrorCode.INVALID_IMAGE_FILE_NAME);
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }
}