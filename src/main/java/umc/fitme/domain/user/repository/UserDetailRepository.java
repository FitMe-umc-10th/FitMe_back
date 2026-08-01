package umc.fitme.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;

import java.util.Optional;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    Optional<UserDetail> findByUser(User user);

    Optional<UserDetail> findByUserId(Long userId);
}