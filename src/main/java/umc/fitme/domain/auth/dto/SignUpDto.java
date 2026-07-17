package umc.fitme.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SignUpDto {

    public record SignUpReq(

            @NotBlank(message = "이름은 필수 값입니다.")
            String name,

//            @NotBlank(message = "생년월일은 필수 값입니다.")
            LocalDate birth,

            @Schema(example = "fitme@example.com")
            @Email(message = "이메일 형식에 맞게 입력해주세요.")
            String email,

            @NotBlank(message = "인증번호 6자리는 필수 값입니다.")
            String verificationCode,

            @NotBlank(message = "비밀번호는 필수 값입니다.")
            String password,

            @NotBlank(message = "비밀번호 검증은 필수 값입니다.")
            String passwordConfirm,

//            @AssertTrue(message = "개인정보 보호 약관 동의는 필수입니다.")
            boolean privacyPolicyAgreed
    ){}

    @Builder
    public record SignUpRes(

            @Schema(example = "fitme@example.com")
            String email,

            LocalDateTime createdAt
    ){}
}
