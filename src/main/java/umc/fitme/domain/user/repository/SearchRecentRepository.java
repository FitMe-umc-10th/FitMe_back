package umc.fitme.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import umc.fitme.domain.user.entity.SearchRecent;

import java.util.List;
import java.util.Optional;

public interface SearchRecentRepository extends JpaRepository<SearchRecent, Long> {

    @Query("""
    select sr
    from SearchRecent sr
    where sr.user.id = :userId
    order by sr.updateAt desc
    limit 10
    """)
    List<SearchRecent> findTop10ByUserIdOrderByUpdateAtDesc(Long userId);

    Optional<SearchRecent> findByUserIdAndKeyword(Long userId, String keyword);

    @Query("""
select sr
from SearchRecent sr
where sr.id = :id
    and sr.user.id = :userId
""")
    Optional<SearchRecent> findByIdAndUserId(Long id, Long userId);
}
