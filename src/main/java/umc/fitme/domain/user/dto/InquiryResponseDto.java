package umc.fitme.domain.user.dto;

import lombok.Builder;

import java.time.LocalDateTime;

public class InquiryResponseDto {

    @Builder
    public record CreateInquiryResponse(
            Long inquiryId,
            String replyEmail,
            LocalDateTime createdAt
    ) {
    }
}