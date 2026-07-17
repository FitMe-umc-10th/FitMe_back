package umc.fitme.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.user.dto.UserApplicationRequestDto;
import umc.fitme.domain.user.dto.UserApplicationResponseDto;
import umc.fitme.domain.user.exception.code.UserApplicationSuccessCode;
import umc.fitme.domain.user.service.UserApplicationService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.security.entity.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-applications")
public class UserApplicationController {

    private final UserApplicationService userApplicationService;

    @PostMapping
    public ApiResponse<UserApplicationResponseDto.CreateResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody UserApplicationRequestDto.CreateRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.create(principal.getUserId(), request)
        );
    }

    @GetMapping
    public ApiResponse<UserApplicationResponseDto.ListResponse> getList(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String tab
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.getList(principal.getUserId(), tab)
        );
    }

    @GetMapping("/{userApplicationId}")
    public ApiResponse<UserApplicationResponseDto.DetailResponse> getDetail(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long userApplicationId
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.getDetail(principal.getUserId(), userApplicationId)
        );
    }

    @PatchMapping("/{userApplicationId}/status")
    public ApiResponse<UserApplicationResponseDto.UpdateStatusResponse> updateStatus(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long userApplicationId,
            @RequestBody UserApplicationRequestDto.UpdateStatusRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.updateStatus(principal.getUserId(), userApplicationId, request)
        );
    }

    @PatchMapping("/{userApplicationId}/memo")
    public ApiResponse<UserApplicationResponseDto.UpdateMemoResponse> updateMemo(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long userApplicationId,
            @RequestBody UserApplicationRequestDto.UpdateMemoRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.updateMemo(principal.getUserId(), userApplicationId, request)
        );
    }

    @DeleteMapping("/{userApplicationId}")
    public ApiResponse<UserApplicationResponseDto.DeleteResponse> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userApplicationId
    ) {
        return ApiResponse.onSuccess(
                UserApplicationSuccessCode.DELETE_USER_APPLICATION_SUCCESS,
                userApplicationService.delete(
                        userDetails.getUserId(),
                        userApplicationId
                )
        );
    }
}