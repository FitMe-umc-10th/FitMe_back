package umc.fitme.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.post.service.PostQueryService;
import umc.fitme.domain.post.service.PublicDataSyncService;
import umc.fitme.domain.post.enums.ClosingSoonSort;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.domain.user.dto.request.UserApplicationRequestDTO;
import umc.fitme.domain.user.dto.response.UserApplicationResponseDTO;
import umc.fitme.domain.user.service.UserApplicationService;
import umc.fitme.global.apiPayload.ApiResponse;
import java.util.List;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostQueryService postQueryService;
    private final PublicDataSyncService publicDataSyncService; // 💡 데이터 동기화를 위한 서비스 추가
    private final UserApplicationService userApplicationService;


    @GetMapping("/popular")
    public ApiResponse<PostResponseDTO.PopularPostListDTO> getPopularPosts(
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", defaultValue = "8") Integer size) { //size 파라미터 추가 (기본값 8)
        PostResponseDTO.PopularPostListDTO response = postQueryService.getPopularPosts(cursor, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }


    @GetMapping("/recent-views")
    public ApiResponse<PostResponseDTO.PostPreviewListDTO> getRecentPosts(
            // 로그인 기능 연결 전 Swagger 테스트를 위한 임시 사용자 식별값
            @RequestParam Long userId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) { //size 파라미터 추가 (기본값 10)
        PostResponseDTO.PostPreviewListDTO response = postQueryService.getRecentPosts(userId, page, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }


    @GetMapping("/closing-soon")
    public ApiResponse<List<PostResponseDTO.PostPreviewDTO>> getClosingSoonPosts(
            // 로그인 기능 연결 후에는 인증 정보에서 사용자 ID를 가져오도록 교체한다.
            @RequestParam Long userId,
            @RequestParam(name = "postType", required = false) PostType postType,
            // 생략해도 홈 화면 정책의 기본 정렬(FIT)을 적용한다.
            @RequestParam(name = "sort", defaultValue = "FIT") ClosingSoonSort sort,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {

        List<PostResponseDTO.PostPreviewDTO> response =
                postQueryService.getClosingSoonPosts(userId, postType, sort, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    /**
     * 장학금 공고 상세 화면 진입 API.
     * 상세 조회와 함께 조회 수 및 최근 조회 이력을 갱신한다.
     */
    @GetMapping("/scholarship/{postId}")
    public ApiResponse<PostResponseDTO.PostDetailDTO> getScholarshipPostDetail(
            @PathVariable Long postId,
            // 로그인 기능 연결 전 Swagger 테스트를 위한 임시 사용자 식별값
            @RequestParam Long userId) {
        PostResponseDTO.PostDetailDTO response = postQueryService.getScholarshipPostDetail(userId, postId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    /**
     * 공식 홈페이지로 이동하기 전에 지원 이력을 생성한다.
     * 이미 같은 공고의 이력이 있으면 중복 생성하지 않고 기존 이력을 반환한다.
     * 외부 URL 이동은 응답의 applicationUrl을 받은 프론트엔드가 수행한다.
     */
    @PatchMapping("/{postId}/application")
    public ApiResponse<UserApplicationResponseDTO.ApplicationResponse> startApplication(
            @PathVariable Long postId,
            // 로그인 연결 전 Swagger 테스트용. 이후 인증 사용자 ID로 교체한다.
            @RequestParam Long userId) {
        UserApplicationResponseDTO.ApplicationResponse response =
                userApplicationService.createApplication(
                        userId,
                        new UserApplicationRequestDTO.CreateRequest(postId)
                );
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @GetMapping("/sync-test")
    public ApiResponse<String> triggerSync(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {

        publicDataSyncService.syncScholarshipData(page, perPage);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "공공데이터 동기화가 성공적으로 실행되었습니다.");
    }
}
