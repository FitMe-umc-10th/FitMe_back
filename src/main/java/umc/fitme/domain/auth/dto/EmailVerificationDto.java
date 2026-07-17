package umc.fitme.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailVerificationDto {

    public record EmailVerificationReqDto(

            @Schema(description = "인증 코드를 받을 이메일 주소", example = "fitme@example.com")
            @NotBlank(message = "이메일 필드는 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email
    ){}

    public record EmailVerificationResDto(

            @Schema(example = "fitme@example.com")
            String email,

            @Schema(example = "300s", description = "인증 코드 만료까지 남은 시간(초)")
            Long expiresInSecondes
    ){}
}
