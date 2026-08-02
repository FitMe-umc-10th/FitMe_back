package umc.fitme.domain.notify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import umc.fitme.domain.notify.dto.NotificationResponseDto;
import umc.fitme.domain.notify.exception.code.NotificationErrorCode;
import umc.fitme.domain.notify.service.NotificationService;
import umc.fitme.global.apiPayload.ApiResponse;
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

    @Operation(summary = "알림 읽음 처리 API", description = "알림 단건을 읽음 상태로 변경하는 API")
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(resolveUserId(principal), notificationId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }

    private Long resolveUserId(PrincipalDetails principal) {
        if (principal == null || principal.getUser() == null) {
            throw new ProjectException(NotificationErrorCode.NOTIFICATION_UNAUTHORIZED);
        }
        return principal.getUser().getId();
    }
}
