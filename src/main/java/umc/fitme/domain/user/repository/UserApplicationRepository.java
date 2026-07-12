package umc.fitme.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.enums.Status;

import java.util.List;
import java.util.Optional;

public interface UserApplicationRepository extends JpaRepository<UserApplication, Long> {
    Optional<UserApplication> findByUserAndPostAndDeletedAtIsNull(User user, Post post);

    @EntityGraph(attributePaths = "post")
    List<UserApplication> findAllByUserAndDeletedAtIsNullAndStatusInOrderByUpdatedAtDescIdDesc(
            User user,
            List<Status> statuses
    );

    Optional<UserApplication> findByIdAndUserAndDeletedAtIsNull(Long id, User user);
}
