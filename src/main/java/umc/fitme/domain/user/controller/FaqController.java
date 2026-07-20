package umc.fitme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.fitme.domain.user.dto.FaqResponseDto;
import umc.fitme.domain.user.service.FaqService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;

@Tag(name = "고객센터", description = "FAQ 등 고객센터 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/faqs")
public class FaqController {

    private final FaqService faqService;

    @Operation(summary = "FAQ 목록 조회", description = "자주 묻는 질문(FAQ) 목록을 조회하는 API")
    @GetMapping
    public ApiResponse<FaqResponseDto.FaqListResponse> getFaqs() {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, faqService.getFaqs());
    }
}