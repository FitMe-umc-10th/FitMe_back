package umc.fitme.domain.notify.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.fitme.domain.notify.entity.Notification;
import umc.fitme.domain.notify.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n
            FROM Notification n
            LEFT JOIN FETCH n.post
            WHERE n.user.id = :userId
              AND n.id < :cursor
            ORDER BY n.id DESC
            """)
    List<Notification> findAllByUserIdBeforeCursor(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    long countByUserIdAndIsReadFalse(Long userId);

    @Query("""
            SELECT COUNT(n) > 0
            FROM Notification n
            WHERE n.user.id = :userId
              AND n.post.id = :postId
              AND n.notificationType = :notificationType
              AND n.createdAt >= :from
            """)
    boolean existsSince(
            @Param("userId") Long userId,
            @Param("postId") Long postId,
            @Param("notificationType") NotificationType notificationType,
            @Param("from") LocalDateTime from
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Notification n
            SET n.isRead = true
            WHERE n.user.id = :userId
              AND n.isRead = false
            """)
    int markAllAsRead(@Param("userId") Long userId);
}
