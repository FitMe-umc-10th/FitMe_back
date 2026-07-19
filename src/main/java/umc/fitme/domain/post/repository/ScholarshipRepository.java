package umc.fitme.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.entity.Scholarship;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

    Optional<Scholarship> findBySourceKey(String sourceKey);

    List<Scholarship> findAllByActiveTrueAndSourceKeyNotIn(Collection<String> sourceKeys);
}
