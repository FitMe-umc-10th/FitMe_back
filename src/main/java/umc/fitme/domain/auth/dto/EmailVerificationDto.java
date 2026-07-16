package umc.fitme.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EmailVerificationDto {

    public record EmailVerificationReqDto(
            @NotBlank(message = "이메일 필드는 필수입니다.")
            @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",
                    message = "이메일 형식이 올바르지 않습니다.")
            String email
    ){}

    public record EmailVerificationResDto(
            String email,
            Long expiredInSecondes
    ){}
}
