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
    List<UserSave> findAllByUserAndIsSavedTrueAndPost_CategoryOrderByIdDesc(
            User user,
            PostType category,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"post"})
    List<UserSave> findAllByUserAndIsSavedTrueAndPost_CategoryAndIdLessThanOrderByIdDesc(
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
        order by p.deadlineDate asc, p.id asc
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
                p.deadlineDate > :deadlineDate
                or (p.deadlineDate = :deadlineDate and p.id > :postId)
              )
        order by p.deadlineDate asc, p.id asc
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
          and p.category = :category
        order by p.deadlineDate asc, p.id asc
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
          and p.category = :category
          and (
                p.deadlineDate > :deadlineDate
                or (p.deadlineDate = :deadlineDate and p.id > :postId)
              )
        order by p.deadlineDate asc, p.id asc
    """)
    List<UserSave> findAllByUserAndIsSavedTrueAndPostCategoryAndDeadlineCursorOrderByDeadlineAsc(
            @Param("user") User user,
            @Param("category") PostType category,
            @Param("deadlineDate") LocalDate deadlineDate,
            @Param("postId") Long postId,
            Pageable pageable
    );
}