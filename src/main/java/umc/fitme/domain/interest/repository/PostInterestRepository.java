package umc.fitme.domain.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import umc.fitme.domain.interest.entity.mapping.PostInterest;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostInterestRepository extends JpaRepository<PostInterest, Long> {

    @Query("""
        select pi.post.id, i.interestName
        from PostInterest pi
        join pi.interest i
        where pi.post.id in :postIds
    """)
    List<Object[]> findPostInterestNamesByPostIds(@Param("postIds") Collection<Long> postIds);
}
