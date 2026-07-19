package umc.fitme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.user.dto.MyPageProfileRequestDto;
import umc.fitme.domain.user.dto.MyPageProfileResponseDto;
import umc.fitme.domain.user.dto.MyPageResponseDto;
import umc.fitme.domain.user.dto.NotificationSettingRequestDto;
import umc.fitme.domain.user.dto.NotificationSettingResponseDto;
import umc.fitme.domain.user.service.MyPageProfileService;
import umc.fitme.domain.user.service.MyPageService;
import umc.fitme.domain.user.service.NotificationSettingService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.security.entity.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    private final MyPageProfileService myPageProfileService;
    private final NotificationSettingService notificationSettingService;

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

    @Tag(name = "마이페이지 - 알림 설정")
    @Operation(summary = "알림 설정 조회", description = "로그인한 사용자의 알림 설정을 조회하는 API")
    @GetMapping("/notification-settings")
    public ApiResponse<NotificationSettingResponseDto.NotificationSettingResponse> getNotificationSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                notificationSettingService.getMyNotificationSetting(userDetails.getUserId()));
    }

    @Tag(name = "마이페이지 - 알림 설정")
    @Operation(summary = "알림 설정 수정", description = "값이 전달된 항목만 부분 수정하는 API")
    @PatchMapping("/notification-settings")
    public ApiResponse<NotificationSettingResponseDto.NotificationSettingResponse> updateNotificationSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NotificationSettingRequestDto.UpdateNotificationSettingRequest request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                notificationSettingService.updateMyNotificationSetting(userDetails.getUserId(), request));
    }
}