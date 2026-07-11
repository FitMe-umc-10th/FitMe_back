package umc.fitme.domain.user.entity.mapping;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.user.entity.User;
import umc.fitme.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "user_save")
public class UserSave extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "is_saved")
    @Builder.Default
    private Boolean isSaved = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public void cancelSave() {
        this.isSaved = false;
    }
}
