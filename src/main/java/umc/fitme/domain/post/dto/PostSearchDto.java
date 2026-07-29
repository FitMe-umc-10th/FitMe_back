package umc.fitme.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
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
    ){
        public PostSearchReq{
            if (type == null) type = PostType.ALL;
            if (sort == null) sort = SearchSortType.DEADLINE;
            if (pageSize == null) pageSize = 10;
        }

        @AssertTrue(message = "공고 타입이 '전체' 혹은 '장학금'이라면, 카테고리에는 값이 포함될 수 없습니다.")
        @JsonIgnore
        public boolean isTypeValid(){
            if (type != PostType.CONTEST && category != null){
                return false;
            }
            return true;
        }

        @AssertTrue(message = "마감순 정렬 시 idCursor와 deadlineCursor는 모두 비어있거나(첫 페이지) 모두 존재해야 합니다(다음 페이지).")
        @JsonIgnore
        public boolean isSortAndCursorValid(){
            if (sort == SearchSortType.DEADLINE){
                boolean isFirstPage = (idCursor == null && deadlineCursor == null);
                boolean isNextPage = (idCursor != null && deadlineCursor != null);

                return isFirstPage || isNextPage;
            }
            return true;
        }
    }

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
