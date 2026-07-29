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
import umc.fitme.domain.user.dto.ProfileImageRequestDto;
import umc.fitme.domain.user.dto.ProfileImageResponseDto;
import umc.fitme.domain.user.service.MyPageProfileService;
import umc.fitme.domain.user.service.MyPageService;
import umc.fitme.domain.user.service.NotificationSettingService;
import umc.fitme.domain.user.service.ProfileImageService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.security.entity.PrincipalDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    private final MyPageProfileService myPageProfileService;
    private final NotificationSettingService notificationSettingService;
    private final ProfileImageService profileImageService;

    @Tag(name = "마이페이지 관련 API")
    @Operation(summary = "마이페이지 대시보드 조회",
            description = "로그인한 사용자의 프로필 요약과 활동 요약(지원 완료 건수·결과 대기 건수·누적 장학금 수혜액)을 조회하는 API")
    @GetMapping
    public ApiResponse<MyPageResponseDto.MyPageResponse> getMyPage(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                myPageService.getMyPage(principal.getUser().getId())
        );
    }

    @Tag(name = "마이페이지 관련 API")
    @Operation(summary = "프로필 정보 조회",
            description = "로그인한 사용자의 프로필과 관심 분야 목록을 조회하는 API")
    @GetMapping("/profile")
    public ApiResponse<MyPageProfileResponseDto.ProfileResponse> getProfile(
            @AuthenticationPrincipal PrincipalDetails principal) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                myPageProfileService.getProfile(principal.getUser().getId()));
    }

    @Tag(name = "마이페이지 관련 API")
    @Operation(summary = "프로필 정보 수정",
            description = "값이 전달된 항목만 부분 수정하는 API. profileImageUrl은 본인 S3 업로드 경로로 시작하는 값만 허용된다.")
    @PatchMapping("/profile")
    public ApiResponse<MyPageProfileResponseDto.UpdateProfileResponse> updateProfile(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody MyPageProfileRequestDto.UpdateProfileRequest request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                myPageProfileService.updateProfile(principal.getUser().getId(), request));
    }

    @Tag(name = "마이페이지 관련 API")
    @Operation(summary = "프로필 이미지 업로드 presigned URL 발급",
            description = "파일명/컨텐츠타입을 받아 S3 PUT presigned URL과 최종 조회 URL을 발급한다. jpg/jpeg/png만 허용. "
                    + "발급받은 uploadUrl로 PUT 업로드 시 요청한 contentType과 동일한 Content-Type 헤더 필수, uploadUrl 유효시간 5분. "
                    + "요청한 fileSize도 서명에 포함되므로 업로드 본문 크기가 정확히 일치해야 한다. (최대 5MB)")
    @PostMapping("/profile/image/presigned-url")
    public ApiResponse<ProfileImageResponseDto.PresignedUrlResponse> issueProfileImagePresignedUrl(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody ProfileImageRequestDto.PresignedUrlRequest request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                profileImageService.createPresignedUrl(principal.getUser().getId(), request));
    }

    @Tag(name = "마이페이지 관련 API")
    @Operation(summary = "알림 설정 조회", description = "로그인한 사용자의 알림 설정을 조회하는 API")
    @GetMapping("/notification-settings")
    public ApiResponse<NotificationSettingResponseDto.NotificationSettingResponse> getNotificationSetting(
            @AuthenticationPrincipal PrincipalDetails principal) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                notificationSettingService.getMyNotificationSetting(principal.getUser().getId()));
    }

    @Tag(name = "마이페이지 관련 API")
    @Operation(summary = "알림 설정 수정", description = "값이 전달된 항목만 부분 수정하는 API")
    @PatchMapping("/notification-settings")
    public ApiResponse<NotificationSettingResponseDto.NotificationSettingResponse> updateNotificationSetting(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody NotificationSettingRequestDto.UpdateNotificationSettingRequest request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                notificationSettingService.updateMyNotificationSetting(principal.getUser().getId(), request));
    }
}