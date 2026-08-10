package umc.fitme.domain.user.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.fitme.domain.notify.dto.DeadlineEmailReminderTarget;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserSave;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserSaveRepository extends JpaRepository<UserSave, Long> {

    // isSaved 전이(false -> true)를 원자적으로 처리하기 위해 행을 잠근 채 읽는다.
    // 락킹 읽기는 REPEATABLE READ 스냅샷이 아니라 최신 커밋 버전을 읽으므로,
    // 대기에서 풀린 트랜잭션은 앞선 트랜잭션이 바꾼 isSaved=true를 그대로 본다.
    // 주의: 행이 없으면 유니크 인덱스에 갭 락이 잡히고, 이 상태에서 두 트랜잭션이
    // 동시에 INSERT하면 데드락이 날 수 있다. savePost()에서 이를 409로 변환한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserSave> findByUserAndPost(User user, Post post);

    @Query("select us.post.id from UserSave us where us.user.id = :userId and us.isSaved = true")
    Set<Long> findSavedPostIdsByUserId(@Param("userId") Long userId);

    // 저장 취소도 isSaved 전이(true -> false)이므로 같은 이유로 잠근 채 읽는다.
    // 여기서는 id로 기존 행을 찾으므로 갭 락이 아니라 레코드 락만 잡힌다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserSave> findByIdAndUserAndIsSavedTrue(Long id, User user);

    @EntityGraph(attributePaths = {"post"})
    List<UserSave> findAllByUserAndIsSavedTrueOrderByIdDesc(
            User user,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"post"})
    List<UserSave> findAllByUserAndIsSavedTrueAndIdLessThanOrderByIdDesc(
            User user,
            Long id,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"post"})
    List<UserSave> findAllByUserAndIsSavedTrueAndPost_PostTypeOrderByIdDesc(
            User user,
            PostType category,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"post"})
    List<UserSave> findAllByUserAndIsSavedTrueAndPost_PostTypeAndIdLessThanOrderByIdDesc(
            User user,
            PostType category,
            Long id,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"post"})
    @Query("""
        select us
        from UserSave us
        join us.post p
        where us.user = :user
          and us.isSaved = true
        order by p.applyEndAt asc, p.id asc
    """)
    List<UserSave> findAllByUserAndIsSavedTrueOrderByDeadlineAsc(
            @Param("user") User user,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"post"})
    @Query("""
        select us
        from UserSave us
        join us.post p
        where us.user = :user
          and us.isSaved = true
          and (
                p.applyEndAt > :deadlineDate
                or (p.applyEndAt = :deadlineDate and p.id > :postId)
              )
        order by p.applyEndAt asc, p.id asc
    """)
    List<UserSave> findAllByUserAndIsSavedTrueAndDeadlineCursorOrderByDeadlineAsc(
            @Param("user") User user,
            @Param("deadlineDate") LocalDate deadlineDate,
            @Param("postId") Long postId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"post"})
    @Query("""
        select us
        from UserSave us
        join us.post p
        where us.user = :user
          and us.isSaved = true
          and p.postType = :category
        order by p.applyEndAt asc, p.id asc
    """)
    List<UserSave> findAllByUserAndIsSavedTrueAndPostCategoryOrderByDeadlineAsc(
            @Param("user") User user,
            @Param("category") PostType category,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"post"})
    @Query("""
        select us
        from UserSave us
        join us.post p
        where us.user = :user
          and us.isSaved = true
          and p.postType = :category
          and (
                p.applyEndAt > :deadlineDate
                or (p.applyEndAt = :deadlineDate and p.id > :postId)
              )
        order by p.applyEndAt asc, p.id asc
    """)
    List<UserSave> findAllByUserAndIsSavedTrueAndPostCategoryAndDeadlineCursorOrderByDeadlineAsc(
            @Param("user") User user,
            @Param("category") PostType category,
            @Param("deadlineDate") LocalDate deadlineDate,
            @Param("postId") Long postId,
            Pageable pageable
    );

    @Query("""
        select new umc.fitme.domain.notify.dto.DeadlineEmailReminderTarget(u, p, uns.notificationEmail)
        from UserSave us
        join us.user u
        join us.post p
        join UserNotificationSetting uns on uns.user = u
        where us.isSaved = true
            and p.applyEndAt in :applyEndDates
            and uns.reminderEnabled = true
    """)
    List<DeadlineEmailReminderTarget> findDeadlineEmailReminderTargets(
            @Param("applyEndDates") List<LocalDate> applyEndDates
    );

    @Query("""
        select us.post.id
        from UserSave us
        where us.user.id = :userId
            and us.post.id in :postIds
            and us.isSaved = true
""")
    Set<Long> findUserSaveIdsByUserIdAndPostIds(Long userId, List<Long> postIds);
}
