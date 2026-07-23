package umc.fitme.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.post.service.PostQueryService;
import umc.fitme.domain.post.service.PublicDataSyncService;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.global.apiPayload.ApiResponse;
import java.util.List;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostQueryService postQueryService;
    private final PublicDataSyncService publicDataSyncService; // 💡 데이터 동기화를 위한 서비스 추가


    @GetMapping("/popular")
    public ApiResponse<PostResponseDTO.PopularPostListDTO> getPopularPosts(
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", defaultValue = "8") Integer size) { //size 파라미터 추가 (기본값 8)
        PostResponseDTO.PopularPostListDTO response = postQueryService.getPopularPosts(cursor, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }


    @GetMapping("/recent-views")
    public ApiResponse<PostResponseDTO.PostPreviewListDTO> getrecentPosts(
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", defaultValue = "10") Integer size) { //size 파라미터 추가 (기본값 10)
        PostResponseDTO.PostPreviewListDTO response = postQueryService.getRecentPosts(cursor, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }


    @GetMapping("/closing-soon")
    public ApiResponse<List<PostResponseDTO.PostPreviewDTO>> getClosingSoonPosts(
            @RequestParam(name = "size", defaultValue = "10") Integer size) {

        List<PostResponseDTO.PostPreviewDTO> response = postQueryService.getClosingSoonPosts(size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @GetMapping("/{postType}/{postId}")
    public ApiResponse<PostResponseDTO.PostDetailDTO> getPostDetail(
            @PathVariable String postType,
            @PathVariable Long postId) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }

    @PatchMapping("/{postId}/application")
    public ApiResponse<String> updateApplicationStatus(@PathVariable Long postId) {

        // TODO: Service 계층에 지원 상태 업데이트 로직 구현 필요
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "지원 상태가 업데이트 되었습니다.");
    }


    @GetMapping("/sync-test")
    public ApiResponse<String> triggerSync(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {

        publicDataSyncService.syncScholarshipData(page, perPage);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "공공데이터 동기화가 성공적으로 실행되었습니다.");
    }
}