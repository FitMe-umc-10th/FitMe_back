package umc.fitme.domain.user.entity.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.PostType;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(
        name = "user_application_post_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_application_post_snapshot_application",
                        columnNames = "user_application_id"
                )
        }
)
public class UserApplicationPostSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_application_id", nullable = false)
    private UserApplication userApplication;

    @Column(name = "original_post_id", nullable = false)
    private Long originalPostId;

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

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "application_method", nullable = false)
    private String applicationMethod;

    @Column(name = "application_url", nullable = false)
    private String applicationUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "grade_requirement")
    private String gradeRequirement;

    @Column(name = "income_requirement")
    private String incomeRequirement;

    @Column(name = "region_requirement")
    private String regionRequirement;

    @Column(name = "support_amount")
    private String supportAmount;

    @Column(name = "support_amount_value")
    private Long supportAmountValue;

    @Column(name = "poster_image_url")
    private String posterImageUrl;

    @Column(name = "target")
    private String target;

    @Column(name = "participant_limit")
    private String participantLimit;

    @Column(name = "reward_total")
    private String rewardTotal;

    public static UserApplicationPostSnapshot from(UserApplication userApplication, Post post) {
        Post unproxiedPost = Hibernate.unproxy(post, Post.class);

        UserApplicationPostSnapshot snapshot = new UserApplicationPostSnapshot();
        snapshot.userApplication = userApplication;
        snapshot.originalPostId = unproxiedPost.getId();
        snapshot.postType = unproxiedPost.getPostType();
        snapshot.title = unproxiedPost.getTitle();
        snapshot.organizer = unproxiedPost.getOrganizer();
        snapshot.applyStartAt = unproxiedPost.getApplyStartAt();
        snapshot.applyEndAt = unproxiedPost.getApplyEndAt();
        snapshot.summary = unproxiedPost.getSummary();
        snapshot.applicationMethod = unproxiedPost.getApplicationMethod();
        snapshot.applicationUrl = unproxiedPost.getApplicationUrl();
        snapshot.imageUrl = unproxiedPost.getImageUrl();

        if (unproxiedPost instanceof Scholarship scholarship) {
            snapshot.gradeRequirement = scholarship.getGradeRequirement();
            snapshot.incomeRequirement = scholarship.getIncomeRequirement();
            snapshot.regionRequirement = scholarship.getRegionRequirement();
            snapshot.supportAmount = scholarship.getSupportAmount();
            snapshot.supportAmountValue = scholarship.getSupportAmountValue();
        }

        if (unproxiedPost instanceof Contest contest) {
            snapshot.posterImageUrl = contest.getPosterImageUrl();
            snapshot.target = contest.getTarget();
            snapshot.participantLimit = contest.getParticipantLimit();
            snapshot.rewardTotal = contest.getRewardTotal();
        }

        return snapshot;
    }
}
