package umc.fitme.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.user.dto.MyPageProfileRequestDto;
import umc.fitme.domain.user.dto.MyPageProfileResponseDto;
import umc.fitme.domain.user.dto.MyPageResponseDto;
import umc.fitme.domain.user.service.MyPageProfileService;
import umc.fitme.domain.user.service.MyPageService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.security.entity.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    private final MyPageProfileService myPageProfileService;

    @GetMapping
    public ApiResponse<MyPageResponseDto.MyPageResponse> getMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                myPageService.getMyPage(userDetails.getUserId())
        );
    }

    @GetMapping("/profile")
    public ApiResponse<MyPageProfileResponseDto.ProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                myPageProfileService.getProfile(userDetails.getUserId()));
    }

    @PatchMapping("/profile")
    public ApiResponse<MyPageProfileResponseDto.UpdateProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MyPageProfileRequestDto.UpdateProfileRequest request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                myPageProfileService.updateProfile(userDetails.getUserId(), request));
    }
}