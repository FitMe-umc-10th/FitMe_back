package umc.fitme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.user.dto.UserApplicationRequestDto;
import umc.fitme.domain.user.dto.UserApplicationResponseDto;
import umc.fitme.domain.user.exception.code.UserApplicationSuccessCode;
import umc.fitme.domain.user.service.UserApplicationService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.security.entity.PrincipalDetails;


@Tag(name = "지원 이력 API", description = "지원 이력 생성, 목록 조회, 상세 조회, 상태 변경, 메모 수정, 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-applications")
public class UserApplicationController {

    private final UserApplicationService userApplicationService;

    @Operation(summary = "지원 이력 생성", description = "공고를 사용자의 지원 이력에 등록하는 API")
    @PostMapping
    public ApiResponse<UserApplicationResponseDto.CreateResponse> create(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody UserApplicationRequestDto.CreateRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.create(principal.getUser().getId(), request)
        );
    }

    @Operation(summary = "지원 이력 목록 조회", description = "탭 조건에 따라 사용자의 지원 이력 목록을 조회하는 API")
    @GetMapping
    public ApiResponse<UserApplicationResponseDto.ListResponse> getList(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(
                    description = "조회할 지원 이력 탭 값",
                    example = "IN_PROGRESS",
                    schema = @Schema(
                            allowableValues = {"IN_PROGRESS", "FINAL_PASSED"}
                    )
            )
            @RequestParam String tab
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.getList(principal.getUser().getId(), tab)
        );
    }

    @Operation(summary = "지원 이력 상세 조회", description = "지원 이력 ID로 사용자의 지원 이력 상세 정보를 조회하는 API")
    @GetMapping("/{userApplicationId}")
    public ApiResponse<UserApplicationResponseDto.DetailResponse> getDetail(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long userApplicationId
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.getDetail(principal.getUser().getId(), userApplicationId)
        );
    }

    @Operation(summary = "지원 이력 상태 변경", description = "지원 이력의 진행 상태를 변경하는 API")
    @PatchMapping("/{userApplicationId}/status")
    public ApiResponse<UserApplicationResponseDto.UpdateStatusResponse> updateStatus(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long userApplicationId,
            @RequestBody UserApplicationRequestDto.UpdateStatusRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.updateStatus(principal.getUser().getId(), userApplicationId, request)
        );
    }

    @Operation(summary = "지원 이력 메모 수정", description = "지원 이력에 작성한 메모를 수정하는 API")
    @PatchMapping("/{userApplicationId}/memo")
    public ApiResponse<UserApplicationResponseDto.UpdateMemoResponse> updateMemo(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long userApplicationId,
            @RequestBody UserApplicationRequestDto.UpdateMemoRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                userApplicationService.updateMemo(principal.getUser().getId(), userApplicationId, request)
        );
    }

    @Operation(summary = "지원 이력 삭제", description = "지원 이력을 삭제하는 API")
    @DeleteMapping("/{userApplicationId}")
    public ApiResponse<UserApplicationResponseDto.DeleteResponse> delete(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long userApplicationId
    ) {
        return ApiResponse.onSuccess(
                UserApplicationSuccessCode.DELETE_USER_APPLICATION_SUCCESS,
                userApplicationService.delete(
                        principal.getUser().getId(),
                        userApplicationId
                )
        );
    }
}