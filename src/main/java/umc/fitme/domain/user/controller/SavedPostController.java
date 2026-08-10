package umc.fitme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.security.entity.PrincipalDetails;

@Tag(name = "공고 찜")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/saved-posts")
public class SavedPostController {

    private final SavedPostService savedPostService;

    @Operation(summary = "찜한 공고 리스트 조회 API", description = "회원이 찜한 공고 목록을 보여준다.")
    @GetMapping
    public ApiResponse<SavedPostResponseDto.SavedPostListResponse> getSavedPosts(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam(defaultValue = "ALL") SavedPostCategory category,
            @RequestParam(defaultValue = "DEADLINE") SavedPostSort sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                savedPostService.getSavedPosts(principal.getUser().getId(), category, sort, cursor, size)
        );
    }

    @Operation(summary = "공고 찜 등록 API", description = "해당 공고를 찜한다.")
    @PostMapping
    public ApiResponse<SavedPostResponseDto.SavePostResponse> savePost(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody SavedPostRequestDto.SavePostRequest request
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                savedPostService.savePost(principal.getUser().getId(), request.postId())
        );
    }

    @Operation(summary = "찜한 공고 삭제 API", description = "찜했던 공고를 삭제한다.")
    @DeleteMapping("/{savedId}")
    public ApiResponse<SavedPostResponseDto.DeleteSavedPostResponse> deleteSavedPost(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long savedId
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                savedPostService.deleteSavedPost(principal.getUser().getId(), savedId)
        );
    }
}
