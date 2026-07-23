package umc.fitme.domain.post.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.fitme.domain.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {


    @Query("SELECT p FROM Post p WHERE (:cursor IS NULL OR p.id < :cursor) ORDER BY p.id DESC")
    List<Post> findPopularPosts(@Param("cursor") Long cursor, PageRequest pageRequest);


    @Query("SELECT p FROM Post p WHERE p.applyEndAt >= CURRENT_DATE ORDER BY p.applyEndAt ASC")
    List<Post> findClosingSoonPosts(PageRequest pageRequest);


    @Query("SELECT p FROM Post p ORDER BY p.savedCount DESC")
    List<Post> findPopularPostsBySavedCount(PageRequest pageRequest);

    @Query("SELECT p FROM Post p ORDER BY RAND()")
    List<Post> findRandomPosts(PageRequest pageRequest);
}