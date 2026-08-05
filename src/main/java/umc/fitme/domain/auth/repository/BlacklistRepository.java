package umc.fitme.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.auth.entity.Blacklist;

import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    Optional<Blacklist> findByToken(String token);
}
