package umc.fitme.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import umc.fitme.domain.user.dto.SavedPostResponseDto;
import umc.fitme.domain.user.enums.SavedPostCategory;
import umc.fitme.domain.user.enums.SavedPostSort;
import umc.fitme.domain.user.service.SavedPostService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/saved-posts")
public class SavedPostController {

    private final SavedPostService savedPostService;

    @GetMapping
    public ApiResponse<SavedPostResponseDto.SavedPostListResponse> getSavedPosts(
            @RequestParam(defaultValue = "ALL") SavedPostCategory category,
            @RequestParam(defaultValue = "RECENT") SavedPostSort sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                savedPostService.getSavedPosts(category, sort, cursor, size)
        );
    }
}
