package umc.fitme.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.enums.SocialType;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialTypeAndSocialUid(SocialType socialType, String socialUid);

    boolean existsByEmail(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long userId);

    Optional<User> findByEmail(String email);
}
