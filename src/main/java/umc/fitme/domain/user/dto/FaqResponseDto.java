package umc.fitme.domain.user.dto;

import lombok.Builder;

import java.util.List;

public class FaqResponseDto {

    @Builder
    public record FaqListResponse(
            List<FaqItem> faqs
    ) {
    }

    @Builder
    public record FaqItem(
            Long faqId,
            String question,
            String answer
    ) {
    }
}