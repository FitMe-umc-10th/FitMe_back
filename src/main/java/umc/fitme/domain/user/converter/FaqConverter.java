package umc.fitme.domain.user.converter;

import umc.fitme.domain.user.dto.FaqResponseDto;
import umc.fitme.domain.user.entity.Faq;

import java.util.List;

public class FaqConverter {

    public static FaqResponseDto.FaqItem toItem(Faq faq) {
        return FaqResponseDto.FaqItem.builder()
                .faqId(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .build();
    }

    public static FaqResponseDto.FaqListResponse toListResponse(List<Faq> faqs) {
        return FaqResponseDto.FaqListResponse.builder()
                .faqs(faqs.stream()
                        .map(FaqConverter::toItem)
                        .toList())
                .build();
    }
}