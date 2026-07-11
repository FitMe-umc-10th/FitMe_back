package umc.fitme.domain.auth.dto;

import jakarta.validation.constraints.NotNull;

public class EmailVerificationConfirmDto {

    public record EmailVerificationConfirmReqDto(

            @NotNull(message = "이메일은 필수입니다.")
            String email,

            @NotNull(message = "인증코드는 필수입니다.")
            String verificationCode
    ){}

    public record EmailVerificationConfirmResDto(
            
            String email,
            boolean isVerified
    ){}
}
