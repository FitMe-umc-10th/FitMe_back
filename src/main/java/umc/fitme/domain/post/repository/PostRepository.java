package umc.fitme.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long>, PostQueryDsl {
}
