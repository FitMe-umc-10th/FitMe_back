package umc.fitme.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.user.dto.MyPageResponseDto;
import umc.fitme.domain.user.service.MyPageService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.security.entity.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ApiResponse<MyPageResponseDto.MyPageResponse> getMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                myPageService.getMyPage(userDetails.getUserId())
        );
    }
}