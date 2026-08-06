package umc.fitme.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.post.dto.PostSearchDto;
import umc.fitme.domain.post.dto.SearchViewDto;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.domain.post.enums.ClosingSoonSort;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.exception.code.PostSuccessCode;
import umc.fitme.domain.post.service.PostQueryService;
import umc.fitme.domain.post.service.PostService;
import umc.fitme.domain.post.service.PostSummaryService;
import umc.fitme.domain.post.sync.service.ScholarshipSyncService;
import umc.fitme.domain.user.dto.UserApplicationRequestDto;
import umc.fitme.domain.user.dto.UserApplicationResponseDto;
import umc.fitme.domain.user.service.UserApplicationService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;
import umc.fitme.global.apiPayload.exception.ProjectException;
import umc.fitme.global.security.entity.PrincipalDetails;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "공고 API", description = "인기 공고 조회/공고 상세 화면/공고 검색/검색 대시보드 조회")
public class PostController {
    private final PostQueryService postQueryService;
    private final UserApplicationService userApplicationService;
    private final PostService postService;
    private final ScholarshipSyncService scholarshipSyncService;
    private final PostSummaryService postSummaryService;


    @GetMapping("/popular")
    public ApiResponse<PostResponseDTO.PopularPostListDTO> getPopularPosts(
            // 비로그인도 인기 공고를 볼 수 있어야 하므로 인증 정보가 없어도 조회를 허용한다.
            // 인증 정보가 없으면 찜 여부는 모두 false로 내려간다.
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", defaultValue = "8") Integer size) { //size 파라미터 추가 (기본값 8)
        PostResponseDTO.PopularPostListDTO response =
                postQueryService.getPopularPosts(resolveUserIdOrNull(principal), cursor, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }


    @GetMapping("/recent-views")
    public ApiResponse<PostResponseDTO.PostPreviewListDTO> getRecentPosts(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) { //size 파라미터 추가 (기본값 10)
        PostResponseDTO.PostPreviewListDTO response =
                postQueryService.getRecentPosts(resolveUserId(principal), page, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }


    @GetMapping("/closing-soon")
    public ApiResponse<List<PostResponseDTO.PostPreviewDTO>> getClosingSoonPosts(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam(name = "postType", required = false) PostType postType,
            // 생략해도 홈 화면 정책의 기본 정렬(FIT)을 적용한다.
            @RequestParam(name = "sort", defaultValue = "FIT") ClosingSoonSort sort,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {

        List<PostResponseDTO.PostPreviewDTO> response =
                postQueryService.getClosingSoonPosts(resolveUserId(principal), postType, sort, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    /**
     * 장학금 공고 상세 화면 진입 API.
     * 상세 조회와 함께 조회 수 및 최근 조회 이력을 갱신한다.
     */
    @GetMapping("/scholarship/{postId}")
    public ApiResponse<PostResponseDTO.PostDetailDTO> getScholarshipPostDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails principal) {
        PostResponseDTO.PostDetailDTO response =
                postQueryService.getScholarshipPostDetail(resolveUserId(principal), postId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    /**
     * 공모전 공고 상세 화면 진입 API.
     * 상세 조회와 함께 조회 수 및 최근 조회 이력을 갱신한다.
     */
    @GetMapping("/contests/{postId}")
    public ApiResponse<PostResponseDTO.PostDetailDTO> getContestPostDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails principal) {
        PostResponseDTO.PostDetailDTO response =
                postQueryService.getContestPostDetail(resolveUserId(principal), postId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    /**
     * 공식 홈페이지로 이동하기 전에 지원 이력을 생성한다.
     * 이미 같은 공고의 이력이 있으면 중복 생성하지 않고 기존 이력을 반환한다.
     * 외부 URL 이동은 응답의 applicationUrl을 받은 프론트엔드가 수행한다.
     */
    @PatchMapping("/{postId}/application")
    public ApiResponse<UserApplicationResponseDto.CreateResponse> startApplication(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails principal) {
        UserApplicationResponseDto.CreateResponse response =
                userApplicationService.create(
                        resolveUserId(principal),
                        new UserApplicationRequestDto.CreateRequest(postId)
                );
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    /**
     * 인증 정보에서 사용자 ID를 꺼낸다.
     * 인증이 필요한 경로에서만 사용하며, 인증 정보가 없으면 401을 던진다.
     */
    private Long resolveUserId(PrincipalDetails principal) {
        if (principal == null || principal.getUser() == null) {
            throw new ProjectException(GeneralErrorCode.UNAUTHORIZED);
        }
        return principal.getUser().getId();
    }

    /**
     * 비로그인 접근을 허용하는 경로에서 사용한다.
     * 인증 정보가 없으면 null을 돌려주고, 서비스는 개인화 정보 없이 응답을 만든다.
     */
    private Long resolveUserIdOrNull(PrincipalDetails principal) {
        return (principal == null || principal.getUser() == null)
                ? null
                : principal.getUser().getId();
    }

    @GetMapping("/scholarship-sync-test")
    public ApiResponse<String> triggerScholarshipSync() {
        scholarshipSyncService.sync();
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "장학금 스케줄러 동기화가 실행되었습니다. scholarship_sync_log에서 결과를 확인하세요.");
    }

    /**
     * AI 공고 요약 생성을 수동으로 트리거하는 테스트용 API.
     * postId가 주어지면 해당 공고 하나만 강제로 재생성하고,
     * 없으면 summary가 비어있는 활성 공고를 배치로 찾아 생성한다.
     */
    @GetMapping("/summary-generate-test")
    public ApiResponse<String> triggerSummaryGeneration(
            @RequestParam(required = false) Long postId) {
        if (postId != null) {
            postSummaryService.generateSummaryForPost(postId);
            return ApiResponse.onSuccess(GeneralSuccessCode.OK, "postId=" + postId + "의 AI 요약이 재생성되었습니다.");
        }

        int count = postSummaryService.generateMissingSummaries();
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, count + "건의 AI 요약이 생성되었습니다.");
    }

    /***
     * 함수 기능: 조건에 맞는 공고를 검색한다.
     * @param dto
     * @return
     */
    @GetMapping("/search-post")
    @Operation(summary = "공고 검색 API", description = "조건에 맞는 공고를 검색한다.")
    public ApiResponse<PostSearchDto.Pagination<PostSearchDto.PostSearchRes>> searchPosts(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @ParameterObject @ModelAttribute PostSearchDto.PostSearchReq dto
    ){
        BaseSuccessCode successCode = PostSuccessCode.SEARCH_POST_OK;
        return ApiResponse.onSuccess(successCode, postService.searchPost(dto, principal.getUser().getId()));
    }

    /***
     * 함수 기능: 검색 대시보드 조회
     * @return
     */
    @GetMapping("/search-main")
    @Operation(summary = "검색 대시보드 조회 API", description = "검색 창을 누르면 나오는 화면이다.")
    public ApiResponse<SearchViewDto.SearchViewRes> getSearchMain(
            @AuthenticationPrincipal PrincipalDetails principal
    ){
        BaseSuccessCode successCode = PostSuccessCode.SEARCH_MAIN_OK;
        return ApiResponse.onSuccess(successCode, postService.getSearchMainPage(principal.getUser().getId()));
    }

    @DeleteMapping("/search/recent/{searchId}")
    @Operation(summary = "나의 검색 기록 삭제 API", description = "최근 검색어 목록에서 X버튼을 누르면 나의 최근검색어 목록에서 사라진다.")
    public ApiResponse<Void> deleteRecentKeyword(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long searchId
    ){
        postService.deleteRecentKeyword(principal.getUser().getId(), searchId);
        BaseSuccessCode successCode = PostSuccessCode.DELETE_RECENT_KEYWORD_OK;
        return ApiResponse.onSuccess(successCode, null);
    }
}
