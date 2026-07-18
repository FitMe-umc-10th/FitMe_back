package umc.fitme.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SignUpResDto (

        @NotBlank(message = "이름은 필수 값입니다.")
        String name,

        @NotBlank(message = "생년월일은 필수 값입니다.")
        LocalDate birth,
        String email,
        String verificationCode,
        String password,
        String passwordConfirm,
        boolean privacyPolicyAgreed
){}
