package umc.fitme.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SignUpDto {

    public record SignUpReq(

            @NotBlank(message = "이름은 필수 값입니다.")
            String name,

            @NotNull(message = "생년월일은 필수 값입니다.")
            @Past(message = "생년월일은 과거 날짜여야 합니다.")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate birth,

            @NotBlank(message = "이메일은 필수 값입니다.")
            @Schema(example = "fitme@example.com")
            @Email(message = "이메일 형식에 맞게 입력해주세요.")
            String email,

            @NotBlank(message = "인증번호 6자리는 필수 값입니다.")
            String verificationCode,

            @NotBlank(message = "비밀번호는 필수 값입니다.")
            @Size(min = 7, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
            @Pattern(
                    regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*]).+$",
                    message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다."
            )
            String password,

            @NotBlank(message = "비밀번호 검증은 필수 값입니다.")
            String passwordConfirm,

            @NotNull(message = "개인정보 보호 약관 동의는 여부는 필수 값입니다.")
            @AssertTrue(message = "개인정보 보호 약관 동의는 필수입니다.")
            boolean privacyPolicyAgreed
    ){
        @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
        @Schema(hidden = true)
        public boolean isPasswordMatching(){
            return password.equals(passwordConfirm);
        }
    }

    @Builder
    public record SignUpRes(

            @Schema(example = "fitme@example.com")
            String email,

            LocalDateTime createdAt
    ){}
}
