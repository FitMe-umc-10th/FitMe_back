package umc.fitme.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.entity.Contest;

public interface ContestRepository extends JpaRepository<Contest, Long> {
    // 일단 냅두고 나중에 공모전 전용 쿼리가 필요할 때 여기에 추가
}