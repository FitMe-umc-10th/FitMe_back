package umc.fitme.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InquiryRequestDto {

    public record CreateInquiryRequest(
            @NotBlank(message = "답변 받을 이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String replyEmail,

            @NotBlank(message = "문의 내용은 필수입니다.")
            @Size(max = 500, message = "문의 내용은 최대 500자까지 입력할 수 있습니다.")
            String content
    ) {
    }
}