package umc.fitme.domain.user.converter;

import umc.fitme.domain.user.dto.InquiryResponseDto;
import umc.fitme.domain.user.entity.Inquiry;
import umc.fitme.domain.user.entity.User;

public class InquiryConverter {

    public static Inquiry toEntity(User user, String replyEmail, String content) {
        return Inquiry.builder()
                .user(user)
                .replyEmail(replyEmail)
                .content(content)
                .build();
    }

    public static InquiryResponseDto.CreateInquiryResponse toResponse(Inquiry inquiry) {
        return InquiryResponseDto.CreateInquiryResponse.builder()
                .inquiryId(inquiry.getId())
                .replyEmail(inquiry.getReplyEmail())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}