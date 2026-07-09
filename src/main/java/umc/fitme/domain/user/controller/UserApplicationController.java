package umc.fitme.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.user.dto.UserApplicationRequestDto;
import umc.fitme.domain.user.dto.UserApplicationResponseDto;
import umc.fitme.domain.user.service.UserApplicationService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-applications")
public class UserApplicationController {

    private final UserApplicationService userApplicationService;

    @PostMapping
    public ApiResponse<UserApplicationResponseDto.CreateResponse> create(
            @RequestBody UserApplicationRequestDto.CreateRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.create(request)
        );
    }

    @GetMapping
    public ApiResponse<UserApplicationResponseDto.ListResponse> getList(
            @RequestParam String tab
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.getList(tab)
        );
    }

    @GetMapping("/{userApplicationId}")
    public ApiResponse<UserApplicationResponseDto.DetailResponse> getDetail(
            @PathVariable Long userApplicationId
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.getDetail(userApplicationId)
        );
    }

    @PatchMapping("/{userApplicationId}/status")
    public ApiResponse<UserApplicationResponseDto.UpdateStatusResponse> updateStatus(
            @PathVariable Long userApplicationId,
            @RequestBody UserApplicationRequestDto.UpdateStatusRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.updateStatus(userApplicationId, request)
        );
    }

    @PatchMapping("/{userApplicationId}/memo")
    public ApiResponse<UserApplicationResponseDto.UpdateMemoResponse> updateMemo(
            @PathVariable Long userApplicationId,
            @RequestBody UserApplicationRequestDto.UpdateMemoRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.updateMemo(userApplicationId, request)
        );
    }

    @DeleteMapping("/{userApplicationId}")
    public ApiResponse<UserApplicationResponseDto.DeleteResponse> delete(
            @PathVariable Long userApplicationId
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.delete(userApplicationId)
        );
    }
}