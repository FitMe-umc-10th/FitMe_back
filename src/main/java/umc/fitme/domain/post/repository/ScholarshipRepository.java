package umc.fitme.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.entity.Scholarship;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

}