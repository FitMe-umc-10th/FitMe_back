package umc.fitme.domain.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserSave;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserSaveRepository extends JpaRepository<UserSave, Long> {

    Optional<UserSave> findByUserAndPost(User user, Post post);

    Optional<UserSave> findByIdAndUser(Long id, User user);

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
        select us
        from UserSave us
        join fetch us.user u
        join fetch us.post p
        join UserNotificationSetting uns on uns.user = u
        where us.isSaved = true
            and p.applyEndAt in :applyEndDates
            and uns.reminderEnabled = true
    """)
    List<UserSave> findDeadlineEmailReminderTargets(
            @Param("applyEndDates") List<LocalDate> applyEndDates
    );
}
