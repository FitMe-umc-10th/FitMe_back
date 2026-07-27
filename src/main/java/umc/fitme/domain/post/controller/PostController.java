package umc.fitme.domain.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.post.dto.PostSearchDto;
import umc.fitme.domain.post.service.PostService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class PostController {

    private final PostService postService;

    /***
     * 함수 기능: 공고를 검색한다.
     * @param dto
     * @return
     */
    @GetMapping("/posts")
    public ApiResponse<PostSearchDto.Pagination<PostSearchDto.PostSearchRes>> searchPosts(
            @ModelAttribute PostSearchDto.PostSearchReq dto
            ){
        BaseSuccessCode successCode = GeneralSuccessCode.OK;
        return ApiResponse.onSuccess(successCode, postService.searchPost(dto, 1L));
    }
}
