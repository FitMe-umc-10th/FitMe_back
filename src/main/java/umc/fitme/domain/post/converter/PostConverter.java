package umc.fitme.domain.post.converter;

import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class PostConverter {

    // 1. 단일 엔티티를 인기 공고 DTO로 변환 (기존 작성 코드)
    public static PostResponseDTO.PopularPostDTO toPopularPostDTO(Post post) {

        //마감일 전환 로직
        Integer dDayLabel = null;
        if (post.getApplyEndAt() != null) {
            dDayLabel = (int) ChronoUnit.DAYS.between(LocalDate.now(), post.getApplyEndAt());
        }

        //포스터 이미지 URL
        String extractedThumbnailUrl = null;
        if (post instanceof Contest) {
            extractedThumbnailUrl = ((Contest) post).getPosterImageUrl();
        } else {
            // 이미지 파일이 없는 경우 기본 이미지 URL을 설정
            extractedThumbnailUrl = "https://default-image-url.com/scholarship.png";
        }

        return PostResponseDTO.PopularPostDTO.builder()
                .postId(post.getId())
                .type(post.getPostType().name())
                .title(post.getTitle())
                .deadlineLabel(dDayLabel)
                .thumbnailUrl(extractedThumbnailUrl)
                .organization(post.getOrganizer())
                .saved(false)
                .build();
    }

    // 2. 리스트 형태와 페이징 정보를 포함한 최종 응답 DTO로 변환 (기존 작성 코드)
    public static PostResponseDTO.PopularPostListDTO toPopularPostListDTO(List<Post> postList, boolean hasNext, Long nextCursor) {
        List<PostResponseDTO.PopularPostDTO> postDTOs = postList.stream()
                .map(PostConverter::toPopularPostDTO)
                .collect(Collectors.toList());

        return PostResponseDTO.PopularPostListDTO.builder()
                .hasNext(hasNext)
                .nextIdCursor(nextCursor)
                .nextDeadlineCursor(null)
                .popularPosts(postDTOs)
                .build();
    }



    //상세 공고 변환 로직

    // 3. 공고 상세 조회 DTO 변환 (평면 데이터 -> 중첩 객체 조립 과정)
    public static PostResponseDTO.PostDetailDTO toPostDetailDTO(Post post) {

        // D-Day 계산
        Integer dDayLabel = null;
        if (post.getApplyEndAt() != null) {
            dDayLabel = (int) ChronoUnit.DAYS.between(LocalDate.now(), post.getApplyEndAt());
        }

        //프론트엔드 장학금 상세 정보
        PostResponseDTO.ScholarshipDetailDTO scholarshipDetailDTO = null;

        // 넘어온 Entity가 장학금(Scholarship) 타입일 때만 내부 로직을 실행
        if (post instanceof Scholarship) {
            Scholarship scholarship = (Scholarship) post; // 자식 클래스로 캐스팅


            // 공공데이터를 받을 때는 PublicApiScholarshipDTO로 평면적으로(Flat) 받았고, DB에도 일렬로 저장되어 있습니다.
            // 하지만 프론트엔드(웹)에게 응답을 줄 때는 노션 API 명세서에 약속된 대로
            // 'scholarshipDetail' 이라는 중첩 객체(Nested Object) 상자에 따로 예쁘게 담아줍니다.
            scholarshipDetailDTO = PostResponseDTO.ScholarshipDetailDTO.builder()
                    .supportAmount(scholarship.getSupportAmount())       // DB에서 꺼내기
                    .gradeRequirement(scholarship.getGradeRequirement()) // DB에서 꺼내기
                    .incomeRequirement(scholarship.getIncomeRequirement()) // DB에서 꺼내기
                    .regionRequirement(scholarship.getRegionRequirement()) // DB에서 꺼내기
                    .build();
        }

        // 최종적으로 가장 바깥쪽 껍데기(PostDetailDTO)를 조립합니다.
        return PostResponseDTO.PostDetailDTO.builder()
                .postId(post.getId())
                .type(post.getPostType().name())
                .title(post.getTitle())
                .organization(post.getOrganizer())
                .viewCount(post.getViewCount())
                .savedCount(post.getSavedCount())
                .saved(false) // 로그인 연동 후 찜 여부 매핑 예정
                .summary(post.getSummary())
                .applyEndDate(post.getApplyEndAt() != null ? post.getApplyEndAt().toString() : null)
                .deadlineLabel(dDayLabel)
                .applicationMethod(post.getApplicationMethod())

                // 위에서 조립한 장학금 전용 박스를 통째로 넣습니다.
                // 만약 공모전(Contest)이었다면 이 값은 null이 되어 명세서 요구사항을 완벽히 충족합니다.
                .scholarshipDetail(scholarshipDetailDTO)
                .build();
    }
}