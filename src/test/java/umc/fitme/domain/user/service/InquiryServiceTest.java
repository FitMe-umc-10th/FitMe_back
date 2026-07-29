package umc.fitme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.user.dto.InquiryRequestDto;
import umc.fitme.domain.user.dto.InquiryResponseDto;
import umc.fitme.domain.user.entity.Inquiry;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.InquiryRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    @DisplayName("1:1 문의를 정상 접수하고 저장된 Inquiry에 요청 값이 매핑된다")
    void createInquiry_success() {
        User user = User.builder().id(1L).build();
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("reply@example.com", "문의 내용입니다.");

        Inquiry savedInquiry = Inquiry.builder()
                .id(100L)
                .user(user)
                .replyEmail("reply@example.com")
                .content("문의 내용입니다.")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);

        InquiryResponseDto.CreateInquiryResponse response = inquiryService.createInquiry(1L, request);

        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository, times(1)).save(captor.capture());

        Inquiry captured = captor.getValue();
        assertEquals(user, captured.getUser());
        assertEquals("reply@example.com", captured.getReplyEmail());
        assertEquals("문의 내용입니다.", captured.getContent());

        assertEquals(100L, response.inquiryId());
        assertEquals("reply@example.com", response.replyEmail());
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다")
    void createInquiry_userNotFound() {
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("reply@example.com", "문의 내용입니다.");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> inquiryService.createInquiry(1L, request)
        );

        assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}