package umc.fitme.domain.post.converter;

import org.jspecify.annotations.NonNull;
import umc.fitme.domain.post.dto.PostSearchDto;
import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.ContestCategory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PostConverter {

    // 선택한 조건에 맞는 Post 객체를 응답 Dto로 변환
    public static PostSearchDto.PostSearchRes toPostSearchRes(Post post, boolean isSaved){

        ContestCategory category = null;
        if (post instanceof Contest contest){
            category = contest.getContestCategory();
        }

        String deadlineLabel = getDeadlineLabel(post);

        return PostSearchDto.PostSearchRes.builder()
                .postId(post.getId())
                .type(post.getPostType())
                .title(post.getTitle())
                .deadlineDate(post.getApplyEndAt())
                .deadlineLabel(deadlineLabel)
                .organization(post.getOrganizer())
                .thumbnailUrl(post.getImageUrl())
                .category(category)
                .saved(isSaved)
                .build();
    }

    // 공고 조회 페이지네이션 틀
    public static <T> PostSearchDto.Pagination<T> toPagination(
            List<T> data,
            Boolean hasNext,
            Long nextIdCursor,
            LocalDate nextDeadlineCursor,
            Integer pageSize){
        return PostSearchDto.Pagination.<T>builder()
                .data(data)
                .hasNext(hasNext)
                .nextIdCursor(nextIdCursor)
                .nextDeadlineCursor(nextDeadlineCursor)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 함수 기능: 마감 날짜의 D-DAY를 계산한다.
     * @param post
     * @return
     */
    private static @NonNull String getDeadlineLabel(Post post) {
        long remainDays = ChronoUnit.DAYS.between(post.getApplyEndAt(), LocalDate.now());
        String deadlineLabel = "";
        if (remainDays == 0){
            deadlineLabel = "D-Day";
        } else {
            deadlineLabel = "D-" + remainDays;
        }
        return deadlineLabel;
    }
}
