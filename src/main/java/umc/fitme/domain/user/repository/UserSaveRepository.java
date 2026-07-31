package umc.fitme.domain.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Optional<UserSave> findByUserAndPost(User user, Post post);

    @Query("select us.post.id from UserSave us where us.user.id = :userId and us.isSaved = true")
    Set<Long> findSavedPostIdsByUserId(@Param("userId") Long userId);

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

    Long user(User user);
}
