package umc.fitme.domain.user.entity.mapping;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.enums.Status;
import umc.fitme.global.entity.BaseEntity;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(
        name = "user_application",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_user_post",
                        columnNames = {"user_id", "post_id"}
                )
        }
)
public class UserApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.NONE;

    @Column(name = "memo", length = 1000)
    private String memo;

    @Column(name = "is_applied", nullable = false)
    @Builder.Default
    private Boolean isApplied = false;

    public void updateMemo(String memo) {
        if (memo == null) {
            this.memo = null;
            return;
        }

        String trimmed = memo.trim();
        this.memo = trimmed.isEmpty() ? null : trimmed;
    }
}
