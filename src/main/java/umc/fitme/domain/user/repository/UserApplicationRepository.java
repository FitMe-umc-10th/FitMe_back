package umc.fitme.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.enums.Status;

import java.util.List;
import java.util.Optional;

public interface UserApplicationRepository extends JpaRepository<UserApplication, Long> {
    Optional<UserApplication> findByUserAndPost(User user, Post post);
    List<UserApplication> findAllByUserAndStatusInOrderByUpdatedAtDescIdDesc(
            User user,
            List<Status> statuses
    );

    Optional<UserApplication> findByIdAndUser(Long id, User user);
}
