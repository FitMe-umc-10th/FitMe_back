package umc.fitme.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import umc.fitme.domain.user.entity.User;
import umc.fitme.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "view_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_view_history_user_post",
                columnNames = {"user_id", "post_id"}
        )
)
public class ViewHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public void updateViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
