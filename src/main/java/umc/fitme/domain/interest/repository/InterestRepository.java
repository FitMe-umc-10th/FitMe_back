package umc.fitme.domain.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umc.fitme.domain.interest.entity.Interest;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByInterestName(String interestName);

    List<Interest> findByInterestNameIn(List<String> interestNames);

    List<Interest> findAllByOrderByIdAsc();
}
