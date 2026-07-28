package umc.fitme.domain.onboarding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.fitme.domain.onboarding.dto.OnboardingRequestDto;
import umc.fitme.domain.onboarding.dto.OnboardingResponseDto;
import umc.fitme.domain.onboarding.exception.code.OnboardingSuccessCode;
import umc.fitme.domain.onboarding.service.OnboardingService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.security.entity.PrincipalDetails;

@RequestMapping("/api/v1/onboarding")
@Tag(name = "온보딩 관련 API")
@RestController
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "온보딩 완료 API", description = "거주지역, 소속대학, 학점, 소득구간, 관심분야를 저장하고 온보딩을 완료 처리하는 API")
    @PostMapping
    public ApiResponse<OnboardingResponseDto> complete(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody OnboardingRequestDto request
    ) {
        return ApiResponse.onSuccess(
                OnboardingSuccessCode.ONBOARDING_COMPLETE,
                onboardingService.complete(principal.getUser().getId(), request)
        );
    }
}
