package umc.fitme.domain.post.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "공고 탐색 API", description = "탐색 배너 API 관련 기능들.")
public class PostController {

    private final PostService postService;

    /***
     * 함수 기능: 조건에 맞는 공고를 검색한다.
     * @param dto
     * @return
     */
    @GetMapping("/posts")
    public ApiResponse<PostSearchDto.Pagination<PostSearchDto.PostSearchRes>> searchPosts(
            @ParameterObject @ModelAttribute PostSearchDto.PostSearchReq dto
            ){
        BaseSuccessCode successCode = GeneralSuccessCode.OK;
        return ApiResponse.onSuccess(successCode, postService.searchPost(dto, 1L));
    }
}
