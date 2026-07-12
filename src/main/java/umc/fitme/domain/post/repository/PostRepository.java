package umc.fitme.domain.post.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.fitme.domain.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 일단은 무작위 순서(최신순) 인기 공고 페이징 조회
    @Query("SELECT p FROM Post p WHERE (:cursor IS NULL OR p.id < :cursor) ORDER BY p.id DESC")
    List<Post> findPopularPosts(@Param("cursor") Long cursor, PageRequest pageRequest);
}