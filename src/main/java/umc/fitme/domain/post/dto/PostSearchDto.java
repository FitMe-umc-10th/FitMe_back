package umc.fitme.domain.post.dto;

import lombok.Builder;
import umc.fitme.domain.post.enums.ContestCategory;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.enums.SearchSortType;

import java.time.LocalDate;
import java.util.List;

public class PostSearchDto {

    public record PostSearchReq(
            PostType type, // 장학금 or 공모전
            List<ContestCategory> category, // 공모전 내 6개 카테고리
            SearchSortType sort, // RECENT or DEADLINE
            String keyword, // 검색어
            Long idCursor, // postId 커서
            LocalDate deadlineCursor, // 데드라인 커서
            Integer pageSize // 보여줄 갯수
    ){}

    @Builder
    public record PostSearchRes(
            Long postId,
            PostType type,
            String title,
            LocalDate deadlineDate,
            String deadlineLabel,
            String organization,
            String thumbnailUrl,
            ContestCategory category,
            Boolean saved
    ){}

    @Builder
    public record Pagination<T>(
            List<T> data,
            Boolean hasNext,
            Long nextIdCursor,
            LocalDate nextDeadlineCursor,
            Integer pageSize // 불러온 데이터 수
    ){}
}
