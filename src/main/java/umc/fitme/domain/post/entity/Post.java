package umc.fitme.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.global.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "post")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostType postType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "organizer", nullable = false)
    private String organizer;

    @Column(name = "apply_start_at", nullable = false)
    private LocalDate applyStartAt;

    @Column(name = "apply_end_at", nullable = false)
    private LocalDate applyEndAt;

    @Column(name = "summary")
    private String summary;

    @Column(name = "application_method", nullable = false)
    private String applicationMethod;

    @Column(name = "application_url", nullable = false)
    private String applicationUrl;

    @Column(name = "view_count")
    private int viewCount = 0;

    public void incrementViewCount() {
        this.viewCount += 1;
    }

    @Column(name = "saved_count")
    private int savedCount = 0;
}