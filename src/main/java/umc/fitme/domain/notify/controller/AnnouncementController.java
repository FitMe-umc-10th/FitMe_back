package umc.fitme.domain.notify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.fitme.domain.notify.dto.AnnouncementResponseDto;
import umc.fitme.domain.notify.service.AnnouncementService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;

@Tag(name = "공지사항 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "공지사항 목록 조회 API", description = "공지사항 목록을 최신순으로 조회하는 API")
    @GetMapping("")
    public ApiResponse<AnnouncementResponseDto.AnnouncementListResponse> getAnnouncements() {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                announcementService.getAnnouncements()
        );
    }

    @Operation(summary = "공지사항 상세 조회 API", description = "공지사항 단건 상세 내용을 조회하는 API")
    @GetMapping("/{announcementId}")
    public ApiResponse<AnnouncementResponseDto.AnnouncementDetailResponse> getAnnouncementDetail(
            @PathVariable Long announcementId
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                announcementService.getAnnouncementDetail(announcementId)
        );
    }
}