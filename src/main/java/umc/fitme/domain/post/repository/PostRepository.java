package umc.fitme.domain.post.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, PostQueryDsl {


    @Query("SELECT p FROM Post p WHERE (:cursor IS NULL OR p.id < :cursor) ORDER BY p.id DESC")
    List<Post> findPopularPosts(@Param("cursor") Long cursor, PageRequest pageRequest);


    // 마감되지 않은 공고를 마감일이 가까운 순서로 조회한다.
    @Query("""
            SELECT p
            FROM Post p
            WHERE p.applyEndAt >= CURRENT_DATE
              AND (:postType IS NULL OR p.postType = :postType)
            ORDER BY p.applyEndAt ASC, p.id ASC
            """)
    List<Post> findClosingSoonPosts(
            @Param("postType") PostType postType,
            PageRequest pageRequest
    );


    // 맞춤 결과가 없을 때 사용할 인기 공고를 찜 횟수 순으로 조회한다.
    @Query("""
            SELECT p
            FROM Post p
            WHERE p.applyEndAt >= CURRENT_DATE
              AND (:postType IS NULL OR p.postType = :postType)
            ORDER BY p.savedCount DESC, p.applyEndAt ASC, p.id ASC
            """)
    List<Post> findPopularPostsBySavedCount(
            @Param("postType") PostType postType,
            PageRequest pageRequest
    );

    // 인기 공고에 노출할 후보를 공고 종류별로 무작위 조회한다.
    // 마감된 공고는 인기 목록에 띄우지 않는다.
    @Query("""
            SELECT p
            FROM Post p
            WHERE p.applyEndAt >= CURRENT_DATE
              AND p.postType = :postType
            ORDER BY RAND()
            """)
    List<Post> findRandomPostsByType(
            @Param("postType") PostType postType,
            PageRequest pageRequest
    );

    @Query("SELECT p FROM Post p ORDER BY RAND()")
    List<Post> findRandomPosts(PageRequest pageRequest);

    @Query("""
    select p
    from Post p
    where p.active = true
      and p.applyEndAt >= CURRENT_DATE
    order by p.viewCount desc, p.id desc
    limit 8
    """)
    List<Post> findTop8ByViewCount();

    @Modifying
    @Query("""
    update Post p
    set p.postRank = -1
    where p.postRank != -1
    """)
    void resetAllRanks();


    // 찜 수는 여러 유저가 공유하는 하나의 post 행을 갱신하므로, 엔티티에서 읽고 더하는
    // 방식(read-modify-write)이면 서로 다른 유저의 동시 찜에서 증가분이 유실된다.
    // DB가 직접 계산하도록 맡겨 갱신 손실을 없앤다.
    // flushAutomatically: 앞선 UserSave 변경을 먼저 반영한 뒤 실행되도록 보장한다.
    // 주의: 벌크 연산이라 영속성 컨텍스트를 우회하므로, 같은 트랜잭션에 이미 로딩된
    // Post 인스턴스의 savedCount는 이 호출 이후 실제 값과 어긋난다.
    @Modifying(flushAutomatically = true)
    @Query("""
    update Post p
    set p.savedCount = p.savedCount + 1
    where p.id = :postId
    """)
    int increaseSavedCount(@Param("postId") Long postId);

    // savedCount > 0 조건으로 음수 방어를 DB에서 원자적으로 수행한다.
    @Modifying(flushAutomatically = true)
    @Query("""
    update Post p
    set p.savedCount = p.savedCount - 1
    where p.id = :postId
      and p.savedCount > 0
    """)
    int decreaseSavedCount(@Param("postId") Long postId);

    // 마감일이 지났는데 아직 활성 상태인 공고를 타입 무관하게 조회한다.
    @Query("SELECT p FROM Post p WHERE p.active = true AND p.applyEndAt < CURRENT_DATE")
    List<Post> findAllActiveAndExpired();

    // AI 요약이 아직 캐싱되지 않은 활성 공고를 타입 무관하게 조회한다 (백필 대상).
    List<Post> findByActiveTrueAndSummaryIsNull(PageRequest pageRequest);
}
