package umc.fitme.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import umc.fitme.domain.user.dto.FaqResponseDto;
import umc.fitme.domain.user.dto.InquiryRequestDto;
import umc.fitme.domain.user.dto.InquiryResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.service.FaqService;
import umc.fitme.domain.user.service.InquiryService;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerServiceControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FaqService faqService;

    @MockitoBean
    private InquiryService inquiryService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtUtil jwtUtil;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // addFilters = false라 필터 체인이 돌지 않으므로 SecurityContextHolder에 직접 인증 정보를 채운다.
    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder) {
        User user = User.builder().id(USER_ID).build();
        PrincipalDetails principal = new PrincipalDetails(user, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return builder;
    }

    @Test
    @DisplayName("FAQ 목록 조회 API 성공")
    void getFaqs_success() throws Exception {
        // given
        FaqResponseDto.FaqListResponse response = FaqResponseDto.FaqListResponse.builder()
                .faqs(List.of(
                        FaqResponseDto.FaqItem.builder()
                                .faqId(1L)
                                .question("회원가입은 어떻게 하나요?")
                                .answer("소셜 로그인으로 가입할 수 있습니다.")
                                .build(),
                        FaqResponseDto.FaqItem.builder()
                                .faqId(2L)
                                .question("비밀번호를 잊어버렸어요.")
                                .answer("소셜 계정으로 로그인해 주세요.")
                                .build()
                ))
                .build();

        given(faqService.getFaqs()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200_1"))
                .andExpect(jsonPath("$.result.faqs.length()").value(2))
                .andExpect(jsonPath("$.result.faqs[0].faqId").value(1))
                .andExpect(jsonPath("$.result.faqs[0].question").value("회원가입은 어떻게 하나요?"))
                .andExpect(jsonPath("$.result.faqs[0].answer").value("소셜 로그인으로 가입할 수 있습니다."))
                .andExpect(jsonPath("$.result.faqs[1].faqId").value(2));
    }

    @Test
    @DisplayName("FAQ가 없으면 빈 목록을 반환한다")
    void getFaqs_emptyList() throws Exception {
        // given
        given(faqService.getFaqs()).willReturn(
                FaqResponseDto.FaqListResponse.builder().faqs(List.of()).build());

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200_1"))
                .andExpect(jsonPath("$.result.faqs.length()").value(0));
    }

    @Test
    @DisplayName("1:1 문의 접수 API 성공 시 201과 CREATED 성공 코드를 반환한다")
    void createInquiry_success() throws Exception {
        // given
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("reply@example.com", "문의 내용입니다.");

        InquiryResponseDto.CreateInquiryResponse response =
                InquiryResponseDto.CreateInquiryResponse.builder()
                        .inquiryId(100L)
                        .replyEmail("reply@example.com")
                        .createdAt(LocalDateTime.of(2026, 7, 28, 12, 0))
                        .build();

        given(inquiryService.createInquiry(anyLong(), any())).willReturn(response);

        // when & then
        mockMvc.perform(withAuth(post("/api/v1/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("201_1"))
                .andExpect(jsonPath("$.message").value("성공적으로 생성이 되었습니다."))
                .andExpect(jsonPath("$.result.inquiryId").value(100))
                .andExpect(jsonPath("$.result.replyEmail").value("reply@example.com"))
                .andExpect(jsonPath("$.result.createdAt").value("2026-07-28T12:00:00"));
    }

    @Test
    @DisplayName("문의 내용이 비어 있으면 400을 반환하고 서비스는 호출되지 않는다")
    void createInquiry_blankContent_returns400() throws Exception {
        // given
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("reply@example.com", "");

        // when & then
        mockMvc.perform(withAuth(post("/api/v1/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result.content").value("문의 내용은 필수입니다."));

        verify(inquiryService, never()).createInquiry(anyLong(), any());
    }

    @Test
    @DisplayName("문의 내용이 500자를 초과하면 400을 반환하고 서비스는 호출되지 않는다")
    void createInquiry_contentTooLong_returns400() throws Exception {
        // given
        String tooLongContent = "가".repeat(501);
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("reply@example.com", tooLongContent);

        // when & then
        mockMvc.perform(withAuth(post("/api/v1/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result.content").value("문의 내용은 최대 500자까지 입력할 수 있습니다."));

        verify(inquiryService, never()).createInquiry(anyLong(), any());
    }

    @Test
    @DisplayName("답변 받을 이메일이 누락되면 400을 반환하고 서비스는 호출되지 않는다")
    void createInquiry_missingReplyEmail_returns400() throws Exception {
        // given
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest(null, "문의 내용입니다.");

        // when & then
        mockMvc.perform(withAuth(post("/api/v1/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result.replyEmail").value("답변 받을 이메일은 필수입니다."));

        verify(inquiryService, never()).createInquiry(anyLong(), any());
    }

    @Test
    @DisplayName("답변 받을 이메일 형식이 올바르지 않으면 400을 반환한다")
    void createInquiry_invalidReplyEmail_returns400() throws Exception {
        // given
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("not-an-email", "문의 내용입니다.");

        // when & then
        mockMvc.perform(withAuth(post("/api/v1/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result.replyEmail").value("이메일 형식이 올바르지 않습니다."));

        verify(inquiryService, never()).createInquiry(anyLong(), any());
    }

    @Test
    @DisplayName("이메일과 문의 내용이 모두 누락되면 두 필드 메시지가 함께 실린다")
    void createInquiry_allFieldsMissing_returns400() throws Exception {
        // given
        String emptyBody = "{}";

        // when & then
        mockMvc.perform(withAuth(post("/api/v1/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result.replyEmail").value("답변 받을 이메일은 필수입니다."))
                .andExpect(jsonPath("$.result.content").value("문의 내용은 필수입니다."));

        verify(inquiryService, never()).createInquiry(anyLong(), any());
    }
}