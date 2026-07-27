package umc.fitme.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.entity.ViewHistory;

import java.util.Optional;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    Optional<ViewHistory> findByUserIdAndPostId(Long userId, Long postId);

    @EntityGraph(attributePaths = "post")
    Page<ViewHistory> findAllByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);
}
