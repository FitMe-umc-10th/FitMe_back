package umc.fitme.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

public class EmailVerificationConfirmDto {

    public record EmailVerificationConfirmReqDto(

            @Schema(description = "인증번호 요청한 이메일 주소", example = "fitme@example.com")
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotBlank(message = "인증코드는 필수입니다.")
            @Pattern(regexp = "\\d{6}", message = "인증코드는 6자리 숫자여야 합니다.")
            String verificationCode
    ){}

    @Builder
    public record EmailVerificationConfirmResDto(

            @Schema(example = "fitme@example.com")
            String email,

            boolean isVerified
    ){}
}
