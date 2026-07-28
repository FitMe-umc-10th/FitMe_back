package umc.fitme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.user.dto.FaqResponseDto;
import umc.fitme.domain.user.entity.Faq;
import umc.fitme.domain.user.repository.FaqRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaqServiceTest {

    @Mock
    private FaqRepository faqRepository;

    @InjectMocks
    private FaqService faqService;

    @Test
    @DisplayName("FAQ 목록을 조회하면 faqId/question/answer가 매핑되고 순서가 유지된다")
    void getFaqs_success() {
        Faq faq1 = Faq.builder().id(1L).question("질문1").answer("답변1").build();
        Faq faq2 = Faq.builder().id(2L).question("질문2").answer("답변2").build();

        when(faqRepository.findAllByOrderByIdAsc()).thenReturn(List.of(faq1, faq2));

        FaqResponseDto.FaqListResponse result = faqService.getFaqs();

        assertEquals(2, result.faqs().size());

        FaqResponseDto.FaqItem first = result.faqs().get(0);
        assertEquals(1L, first.faqId());
        assertEquals("질문1", first.question());
        assertEquals("답변1", first.answer());

        FaqResponseDto.FaqItem second = result.faqs().get(1);
        assertEquals(2L, second.faqId());
        assertEquals("질문2", second.question());
        assertEquals("답변2", second.answer());
    }

    @Test
    @DisplayName("FAQ가 없으면 빈 목록을 반환한다")
    void getFaqs_empty() {
        when(faqRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        FaqResponseDto.FaqListResponse result = faqService.getFaqs();

        assertTrue(result.faqs().isEmpty());
    }
}