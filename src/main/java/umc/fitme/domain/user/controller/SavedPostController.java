package umc.fitme.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.user.dto.SavedPostRequestDto;
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

    @PostMapping
    public ApiResponse<SavedPostResponseDto.SavePostResponse> savePost(
            @Valid @RequestBody SavedPostRequestDto.SavePostRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                savedPostService.savePost(request.postId())
        );
    }

    @DeleteMapping("/{savedId}")
    public ApiResponse<SavedPostResponseDto.DeleteSavedPostResponse> deleteSavedPost(
            @PathVariable Long savedId
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                savedPostService.deleteSavedPost(savedId)
        );
    }
}
