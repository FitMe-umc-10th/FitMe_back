package umc.fitme.domain.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class EmailVerificationConfirmDto {

    public record EmailVerificationConfirmReqDto(

            @NotNull(message = "이메일은 필수입니다.")
            @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\\\.)+[a-zA-Z]{2,7}$",
                    message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotNull(message = "인증코드는 필수입니다.")
            String verificationCode
    ){}

    public record EmailVerificationConfirmResDto(
            
            String email,
            boolean isVerified
    ){}
}
