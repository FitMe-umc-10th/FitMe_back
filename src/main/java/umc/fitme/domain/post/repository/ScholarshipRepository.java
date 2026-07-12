package umc.fitme.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.post.entity.Scholarship;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {
    // 일단 냅두고. 나중에 장학금 전용 쿼리가 필요할 때 여기에 추가 예정
}