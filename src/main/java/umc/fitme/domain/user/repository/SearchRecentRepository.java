package umc.fitme.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import umc.fitme.domain.user.entity.SearchRecent;

import java.util.List;

public interface SearchRecentRepository extends JpaRepository<SearchRecent, Long> {

    @Query("""
    select sr
    from SearchRecent sr
    where sr.user.id = :userId
    order by sr.id desc
    limit 8
    """)
    List<SearchRecent> findTop10ByUserId(Long userId);
}
