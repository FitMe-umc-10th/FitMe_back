package umc.fitme.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.fitme.domain.post.enums.PostType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "post")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "dtype",
        discriminatorType = DiscriminatorType.STRING
)
public class Post {

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

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "view_count")
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "saved_count")
    @Builder.Default
    private int savedCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void increaseViewCount() {
        this.viewCount += 1;
    }
}
