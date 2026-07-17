package umc.fitme.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.entity.mapping.UserApplicationPostSnapshot;

import java.util.Optional;

public interface UserApplicationPostSnapshotRepository extends JpaRepository<UserApplicationPostSnapshot, Long> {

    Optional<UserApplicationPostSnapshot> findByUserApplication(UserApplication userApplication);
}
