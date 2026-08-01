package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.user.converter.FaqConverter;
import umc.fitme.domain.user.dto.FaqResponseDto;
import umc.fitme.domain.user.repository.FaqRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public FaqResponseDto.FaqListResponse getFaqs() {
        return FaqConverter.toListResponse(faqRepository.findAllByOrderByIdAsc());
    }
}