package umc.fitme.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.entity.Contest;

public interface ContestRepository extends JpaRepository<Contest, Long> {

}