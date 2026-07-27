package umc.fitme.domain.post.repository;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import umc.fitme.domain.post.dto.PostSearchDto;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.QContest;
import umc.fitme.domain.post.entity.QPost;
import umc.fitme.domain.post.enums.ContestCategory;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.enums.SearchSortType;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostQueryDslImpl implements PostQueryDsl{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Post> searchPostByCondition(PostSearchDto.PostSearchReq condition) {

        // Q클래스 선언
        QPost post = QPost.post;

        return jpaQueryFactory
                .selectFrom(post)
                .where(
                        typeEq(condition.type()), // 타입 조건
                        categoryEq(condition.category()), // 공모전 카테고리 조건
                        keywordContains(condition.keyword()), // 검색 키워드 조건
                        cursorCondition(condition.sort(), condition.idCursor(), condition.deadlineCursor())
                )
                .orderBy(getOrderSpecifiers(condition.sort()))
                .limit(condition.pageSize()+1)
                .fetch();
    }

    private BooleanExpression typeEq(PostType postType){
        if (postType == null || postType == PostType.ALL){
            return null;
        }
        return QPost.post.postType.eq(postType);
    }

    private BooleanExpression categoryEq(List<ContestCategory> category) {
        if (category == null || category.isEmpty()){
            return null;
        }
        QContest contest = QPost.post.as(QContest.class);
        return contest.contestCategory.in(category);
    }

    private BooleanExpression keywordContains(String keyword){
        if (keyword == null || keyword.isBlank()){
            return null;
        }
        return QPost.post.title.containsIgnoreCase(keyword);
    }

    private BooleanExpression cursorCondition(SearchSortType sortType, Long idCursor, LocalDate deadlineCursor) {

        // 마감일순일 경우
        if (sortType == SearchSortType.DEADLINE || sortType == null){
            if (idCursor == null){ // idCursor가 null일 경우 마감일이 오늘 날짜보다 같거나 큰 공고 반환
                return QPost.post.applyEndAt.goe(LocalDate.now());
            } else { // idCursor와 deadlineCursor가 존재할 경우,
                // deadlineCursor보다 값이 크거나, 같다면 idCursor보다 값이 작은 공고 반환
                return QPost.post.applyEndAt.gt(deadlineCursor)
                        .or(QPost.post.applyEndAt.eq(deadlineCursor).and(QPost.post.id.lt(idCursor)));
            }
        }

        else { // 최신순일 경우
            if (idCursor == null){ // idCursor가 null일 경우 마감일이 오늘 날짜보다 같거나 큰 공고 반환
                return QPost.post.applyEndAt.goe(LocalDate.now());
            } else { // idCursor가 있을 경우, 해당 id보다 작고, 마감일이 오늘 날짜보다 같거나 큰 공고 반환
                return QPost.post.id.lt(idCursor).and(QPost.post.applyEndAt.goe(LocalDate.now()));
            }
        }
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(SearchSortType sortType){
        QPost post = QPost.post;

        // 마감일 정렬일 경우, 1순위: 마감일 오름차순, 2순위: id 내림차순
        if (sortType == SearchSortType.DEADLINE || sortType == null){
            return new OrderSpecifier[]{
                    post.applyEndAt.asc(),
                    post.id.desc()
            };
        }

        // 최신순 정렬일 경우, 1순위: id 내림차순
        else {
            return new OrderSpecifier[]{
                    post.id.desc()
            };
        }
    }
}
