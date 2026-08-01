package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.user.converter.InquiryConverter;
import umc.fitme.domain.user.dto.InquiryRequestDto;
import umc.fitme.domain.user.dto.InquiryResponseDto;
import umc.fitme.domain.user.entity.Inquiry;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.InquiryRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    public InquiryResponseDto.CreateInquiryResponse createInquiry(Long userId, InquiryRequestDto.CreateInquiryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        Inquiry inquiry = InquiryConverter.toEntity(user, request.replyEmail(), request.content());
        Inquiry saved = inquiryRepository.save(inquiry);

        return InquiryConverter.toResponse(saved);
    }
}