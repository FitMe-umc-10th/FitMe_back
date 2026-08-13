package umc.fitme.domain.notify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import umc.fitme.domain.notify.dto.NotificationResponseDto;
import umc.fitme.domain.notify.service.NotificationService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.apiPayload.exception.ProjectException;
import umc.fitme.global.security.entity.PrincipalDetails;

@Tag(name = "알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deadline-notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회 API", description = "본인의 알림 목록을 최신순으로 조회하는 API")
    @GetMapping("")
    public ApiResponse<NotificationResponseDto.NotificationListResponse> getNotifications(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", defaultValue = "15") Integer size
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                notificationService.getNotifications(resolveUserId(principal), cursor, size)
        );
    }

    @Operation(summary = "미읽음 알림 개수 조회 API", description = "알림 벨 배지에 표시할 미읽음 알림 개수를 조회하는 API")
    @GetMapping("/unread-count")
    public ApiResponse<NotificationResponseDto.UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                notificationService.getUnreadCount(resolveUserId(principal))
        );
    }

    /**
     * 인증 정보에서 사용자 ID를 꺼낸다.
     * 인증이 필요한 경로에서만 사용하며, 인증 정보가 없으면 401을 던진다.
     * (시큐리티 필터가 먼저 막아주므로 실제로는 도달하지 않는 방어 코드이며,
     *  CustomEntryPoint 와 같은 코드를 쓰도록 맞춘다.)
     */
    private Long resolveUserId(PrincipalDetails principal) {
        if (principal == null || principal.getUser() == null) {
            throw new ProjectException(GeneralErrorCode.UNAUTHORIZED);
        }
        return principal.getUser().getId();
    }
}
