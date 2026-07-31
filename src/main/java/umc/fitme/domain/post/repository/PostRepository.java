package umc.fitme.domain.post.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {


    @Query("SELECT p FROM Post p WHERE (:cursor IS NULL OR p.id < :cursor) ORDER BY p.id DESC")
    List<Post> findPopularPosts(@Param("cursor") Long cursor, PageRequest pageRequest);


    // 마감되지 않은 공고를 마감일이 가까운 순서로 조회한다.
    @Query("""
            SELECT p
            FROM Post p
            WHERE p.applyEndAt >= CURRENT_DATE
              AND (:postType IS NULL OR p.postType = :postType)
            ORDER BY p.applyEndAt ASC, p.id ASC
            """)
    List<Post> findClosingSoonPosts(
            @Param("postType") PostType postType,
            PageRequest pageRequest
    );


    // 맞춤 결과가 없을 때 사용할 인기 공고를 찜 횟수 순으로 조회한다.
    @Query("""
            SELECT p
            FROM Post p
            WHERE p.applyEndAt >= CURRENT_DATE
              AND (:postType IS NULL OR p.postType = :postType)
            ORDER BY p.savedCount DESC, p.applyEndAt ASC, p.id ASC
            """)
    List<Post> findPopularPostsBySavedCount(
            @Param("postType") PostType postType,
            PageRequest pageRequest
    );

    // 인기 공고에 노출할 후보를 공고 종류별로 무작위 조회한다.
    // 마감된 공고는 인기 목록에 띄우지 않는다.
    @Query("""
            SELECT p
            FROM Post p
            WHERE p.applyEndAt >= CURRENT_DATE
              AND p.postType = :postType
            ORDER BY RAND()
            """)
    List<Post> findRandomPostsByType(
            @Param("postType") PostType postType,
            PageRequest pageRequest
    );
}      
