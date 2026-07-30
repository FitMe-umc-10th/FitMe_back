package umc.fitme.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class LoginDto {

    public record LoginReq(

            @Schema(example = "fitme1234@email.com")
            @Email(message = "이메일 형식에 맞게 입력해주세요.")
            @NotBlank(message = "이메일은 필수 값입니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수값 입니다.")
            String password,

            boolean keepLogin
    ){}

    @Builder
    public record LoginRes(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Member member
    ){
        @Builder
        public record Member(
                Long memberId,
                String email,
                String name,
                boolean isOnboarded
        ){}
    }
}
