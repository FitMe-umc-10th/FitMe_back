package umc.fitme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.user.dto.FaqResponseDto;
import umc.fitme.domain.user.dto.InquiryRequestDto;
import umc.fitme.domain.user.dto.InquiryResponseDto;
import umc.fitme.domain.user.service.FaqService;
import umc.fitme.domain.user.service.InquiryService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.security.entity.CustomUserDetails;

@Tag(name = "고객센터", description = "FAQ 조회 및 1:1 문의 등 고객센터 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CustomerServiceController {

    private final FaqService faqService;
    private final InquiryService inquiryService;

    // 자주 묻는 질문(FAQ) 목록 조회
    @Operation(summary = "FAQ 목록 조회", description = "자주 묻는 질문(FAQ) 목록을 조회하는 API")
    @GetMapping("/faqs")
    public ApiResponse<FaqResponseDto.FaqListResponse> getFaqs() {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, faqService.getFaqs());
    }

    // 1:1 문의 접수
    @Operation(summary = "1:1 문의 접수", description = "고객센터 1:1 문의를 접수하는 API")
    @PostMapping("/inquiries")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InquiryResponseDto.CreateInquiryResponse> createInquiry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InquiryRequestDto.CreateInquiryRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.CREATED,
                inquiryService.createInquiry(userDetails.getUserId(), request)
        );
    }
}