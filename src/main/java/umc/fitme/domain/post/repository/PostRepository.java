package umc.fitme.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import umc.fitme.domain.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, PostQueryDsl {

    @Query("""
    select p
    from Post p
    order by p.viewCount desc
    limit 8
    """)
    List<Post> findTop8ByViewCount();

    @Modifying
    @Query("""
    update Post p
    set p.postRank = -1
    where p.postRank != -1
    """)
    void resetAllRanks();
}
