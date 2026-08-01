package umc.fitme.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "search_recent")
public class SearchRecent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateAt;

    @Builder
    public SearchRecent(User user, String keyword, LocalDateTime updateAt) {
        this.user = user;
        this.keyword = keyword;
        this.updateAt = updateAt;
    }

    public void updateSearchTime() {
        this.updateAt = LocalDateTime.now();
    }
}
