package umc.fitme.domain.notify.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import umc.fitme.domain.notify.dto.AppliedPostReminderTarget;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserNotificationSetting;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.enums.Status;
import umc.fitme.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AppliedPostReminderRepositoryTest extends RepositoryTestSupport {

    private static final LocalDate DEADLINE = LocalDate.of(2026, 8, 7);

    @Autowired
    private AppliedPostReminderRepository appliedPostReminderRepository;

    @Autowired
    private TestEntityManager em;

    private User persistUser(String email, boolean reminderEnabled) {
        User user = User.builder()
                .email(email)
                .build();
        em.persist(user);

        UserNotificationSetting setting = UserNotificationSetting.builder()
                .user(user)
                .notificationEmail(email)
                .pushEnabled(reminderEnabled)
                .recommendedEnabled(false)
                .reminderEnabled(reminderEnabled)
                .build();
        em.persist(setting);

        return user;
    }

    private Post persistPost(String title, LocalDate applyEndAt) {
        Scholarship post = Scholarship.builder()
                .postType(PostType.SCHOLARSHIP)
                .title(title)
                .organizer("테스트 기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(applyEndAt)
                .applicationMethod("홈페이지 지원")
                .applicationUrl("https://example.com")
                .imageUrl("https://example.com/image.png")
                .supportAmount("연 350만원")
                .build();
        em.persist(post);
        return post;
    }

    private UserApplication persistApplication(User user, Post post, Status status, boolean deleted) {
        UserApplication application = UserApplication.builder()
                .user(user)
                .post(post)
                .status(status)
                .isApplied(status != Status.NONE)
                .deletedAt(deleted ? LocalDateTime.of(2026, 7, 30, 12, 0) : null)
                .build();
        em.persist(application);
        return application;
    }

    @Test
    @DisplayName("지원 이력이 있고 알림이 켜져 있으면 마감 임박 알림 대상으로 조회된다")
    void findDeadlineReminderTargets_returnsAppliedTarget() {
        User user = persistUser("applied@example.com", true);
        Post post = persistPost("국가장학금 1유형", DEADLINE);
        persistApplication(user, post, Status.PENDING_RESULT, false);

        em.flush();
        em.clear();

        List<AppliedPostReminderTarget> targets =
                appliedPostReminderRepository.findDeadlineReminderTargets(List.of(DEADLINE));

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().user().getId()).isEqualTo(user.getId());
        assertThat(targets.getFirst().post().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("지원하기만 누르고 상태가 없는 이력도 알림 대상으로 조회된다")
    void findDeadlineReminderTargets_includesApplicationWithoutStatus() {
        User user = persistUser("applied@example.com", true);
        Post post = persistPost("국가장학금 1유형", DEADLINE);
        persistApplication(user, post, Status.NONE, false);

        em.flush();
        em.clear();

        assertThat(appliedPostReminderRepository.findDeadlineReminderTargets(List.of(DEADLINE)))
                .hasSize(1);
    }

    @Test
    @DisplayName("알림 설정을 끈 사용자는 지원 이력이 있어도 조회되지 않는다")
    void findDeadlineReminderTargets_excludesReminderDisabledUser() {
        User user = persistUser("off@example.com", false);
        Post post = persistPost("국가장학금 1유형", DEADLINE);
        persistApplication(user, post, Status.PENDING_RESULT, false);

        em.flush();
        em.clear();

        assertThat(appliedPostReminderRepository.findDeadlineReminderTargets(List.of(DEADLINE)))
                .isEmpty();
    }

    @Test
    @DisplayName("이력에서 삭제한 지원은 조회되지 않는다")
    void findDeadlineReminderTargets_excludesDeletedApplication() {
        User user = persistUser("applied@example.com", true);
        Post post = persistPost("국가장학금 1유형", DEADLINE);
        persistApplication(user, post, Status.PENDING_RESULT, true);

        em.flush();
        em.clear();

        assertThat(appliedPostReminderRepository.findDeadlineReminderTargets(List.of(DEADLINE)))
                .isEmpty();
    }

    @Test
    @DisplayName("알림 대상 날짜가 아닌 마감일은 조회되지 않는다")
    void findDeadlineReminderTargets_excludesOtherDeadline() {
        User user = persistUser("applied@example.com", true);
        Post post = persistPost("국가장학금 1유형", DEADLINE.plusDays(2));
        persistApplication(user, post, Status.PENDING_RESULT, false);

        em.flush();
        em.clear();

        assertThat(appliedPostReminderRepository.findDeadlineReminderTargets(List.of(DEADLINE)))
                .isEmpty();
    }
}
