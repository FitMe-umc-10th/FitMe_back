package umc.fitme.domain.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umc.fitme.domain.interest.entity.mapping.UserInterest;
import umc.fitme.domain.user.entity.User;

import java.util.List;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findAllByUser(User user);
    void deleteAllByUser(User user);
}
