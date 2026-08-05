package umc.fitme.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProfileImageRequestDto {

    /* 프로필 이미지 presigned URL 발급 요청 */
    public record PresignedUrlRequest(
            @NotBlank(message = "파일 이름은 필수입니다.")
            String fileName,

            @NotBlank(message = "파일 형식(Content-Type)은 필수입니다.")
            String contentType,

            @NotNull(message = "파일 용량은 필수입니다.")
            Long fileSize
    ) {
    }
}