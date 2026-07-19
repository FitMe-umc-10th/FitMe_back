package umc.fitme.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umc.fitme.domain.auth.entity.EmailVerification;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByEmailOrderByIdDesc(String email);
}
