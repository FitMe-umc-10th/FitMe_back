
package umc.fitme.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.post.service.PostQueryService;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.global.apiPayload.ApiResponse;

//GET /api/v1/post/popular (실시간 인기 공고)
//GET /api/v1/post/closing-soon (마감 임박 공고)
//GET /api/v1/post/scholarship/{postId}, GET /api/v1/post/contests/{postId} (상세 조회)
//PATCH /api/v1/post/{postId}/application (지원 상태 업데이트)

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostQueryService postQueryService;

    // 실시간 인기 공고 조회
    // GET /api/v1/post/popular
    @GetMapping("/popular")
    public ApiResponse<PostResponseDTO.PopularPostListDTO> getPopularPosts(
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", defaultValue = "8") Integer size) { //size 파라미터 추가 (기본값 8)
        PostResponseDTO.PopularPostListDTO response = postQueryService.getPopularPosts(cursor, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
    //최근 조회 목록 조회
    //


    // 마감 임박 조회 공고
    //GET /api/v1/post/closing-soon

    // 공고 상세 조회
    //GET /api/v1/post/scholarship/{postId}
    //GET /api/v1/post/contests/{postId}

    //지원 상태 업데이트
}