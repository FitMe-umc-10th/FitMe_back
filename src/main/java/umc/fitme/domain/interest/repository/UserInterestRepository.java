package umc.fitme.domain.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umc.fitme.domain.interest.entity.mapping.UserInterest;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
}
