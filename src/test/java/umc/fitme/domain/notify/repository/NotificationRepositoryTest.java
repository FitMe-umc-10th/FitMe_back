package umc.fitme.domain.notify.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import umc.fitme.domain.notify.entity.Notification;
import umc.fitme.domain.notify.enums.NotificationType;
import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.ContestCategory;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.User;
import umc.fitme.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRepositoryTest extends RepositoryTestSupport {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 31, 12, 0, 0);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestEntityManager em;

    private User persistUser(String email) {
        User user = User.builder()
                .email(email)
                .build();
        em.persist(user);
        return user;
    }

    private Post persistPost(String title) {
        Scholarship post = Scholarship.builder()
                .postType(PostType.SCHOLARSHIP)
                .title(title)
                .organizer("테스트 기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 8, 7))
                .applicationMethod("홈페이지 지원")
                .applicationUrl("https://example.com")
                .imageUrl("https://example.com/image.png")
                .supportAmount("연 350만원")
                .createdAt(NOW)
                .build();
        em.persist(post);
        return post;
    }

    private Post persistContest(String title) {
        Contest post = Contest.builder()
                .postType(PostType.CONTEST)
                .contestCategory(ContestCategory.LANGAUGE)
                .title(title)
                .organizer("테스트 기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 8, 7))
                .applicationMethod("홈페이지 지원")
                .applicationUrl("https://example.com")
                .imageUrl("https://example.com/image.png")
                .posterImageUrl("https://example.com/poster.png")
                .createdAt(NOW)
                .build();
        em.persist(post);
        return post;
    }

    private Notification persistNotification(
            User user,
            Post post,
            NotificationType type,
            boolean isRead,
            LocalDateTime createdAt
    ) {
        Notification notification = Notification.builder()
                .user(user)
                .post(post)
                .title("마감 7일 전")
                .message("'%s' 공고의 마감일이 7일 남았습니다.".formatted(post.getTitle()))
                .notificationType(type)
                .isRead(isRead)
                .build();
        em.persist(notification);
        ReflectionTestUtils.setField(notification, "createdAt", createdAt);
        em.flush();
        return notification;
    }

    @Test
    @DisplayName("알림 목록을 최신순으로 조회하고 본인 알림만 내려준다")
    void findAllByUserIdBeforeCursor_returnsOwnNotificationsInLatestOrder() {
        User user = persistUser("owner@example.com");
        User other = persistUser("other@example.com");
        Post post = persistPost("국가장학금 1유형");

        persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW.minusDays(2));
        Notification recent =
                persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW.minusHours(1));
        persistNotification(other, post, NotificationType.LAST_MINUTE, false, NOW);

        em.clear();

        List<Notification> result = notificationRepository.findAllByUserIdBeforeCursor(
                user.getId(), Long.MAX_VALUE, PageRequest.of(0, 10));

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(recent.getId());
    }

    @Test
    @DisplayName("목록 조회 시 공고를 함께 가져와 공고 종류를 추가 조회 없이 읽을 수 있다")
    void findAllByUserIdBeforeCursor_fetchesPost() {
        User user = persistUser("owner@example.com");

        Post post = persistContest("공모전 테스트");
        persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW);

        em.clear();

        List<Notification> result = notificationRepository.findAllByUserIdBeforeCursor(
                user.getId(), Long.MAX_VALUE, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPost().getPostType()).isEqualTo(PostType.CONTEST);
    }

    @Test
    @DisplayName("읽지 않은 알림 개수만 센다")
    void countByUserIdAndIsReadFalse_countsUnreadOnly() {
        User user = persistUser("owner@example.com");
        Post post = persistPost("국가장학금 1유형");

        persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW.minusHours(2));
        persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW.minusHours(1));
        persistNotification(user, post, NotificationType.LAST_MINUTE, true, NOW);

        em.clear();

        assertThat(notificationRepository.countByUserIdAndIsReadFalse(user.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("기준 시각 이후에 만들어진 같은 알림이 있으면 존재로 판정한다")
    void existsSince_detectsSameDayNotification() {
        User user = persistUser("owner@example.com");
        Post post = persistPost("국가장학금 1유형");
        LocalDate today = NOW.toLocalDate();

        persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW);
        em.clear();

        boolean exists = notificationRepository.existsSince(
                user.getId(), post.getId(), NotificationType.LAST_MINUTE, today.atStartOfDay());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("기준 시각 이전에 만들어진 알림은 중복으로 보지 않는다")
    void existsSince_ignoresOlderNotification() {
        User user = persistUser("owner@example.com");
        Post post = persistPost("국가장학금 1유형");
        LocalDate today = NOW.toLocalDate();

        persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW.minusDays(1));
        em.clear();

        boolean exists = notificationRepository.existsSince(
                user.getId(), post.getId(), NotificationType.LAST_MINUTE, today.atStartOfDay());

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("전체 읽음 처리하면 본인의 안 읽은 알림이 모두 읽음이 된다")
    void markAllAsRead_updatesAllUnreadNotifications() {
        User user = persistUser("owner@example.com");
        Post post = persistPost("국가장학금 1유형");

        persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW.minusHours(2));
        persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW.minusHours(1));
        persistNotification(user, post, NotificationType.LAST_MINUTE, true, NOW);

        em.clear();

        int updated = notificationRepository.markAllAsRead(user.getId());

        assertThat(updated).isEqualTo(2);
        assertThat(notificationRepository.countByUserIdAndIsReadFalse(user.getId())).isZero();
    }

    @Test
    @DisplayName("전체 읽음 처리는 다른 사용자의 알림을 건드리지 않는다")
    void markAllAsRead_doesNotTouchOtherUsersNotifications() {
        User user = persistUser("owner@example.com");
        User other = persistUser("other@example.com");
        Post post = persistPost("국가장학금 1유형");

        persistNotification(user, post, NotificationType.LAST_MINUTE, false, NOW);
        persistNotification(other, post, NotificationType.LAST_MINUTE, false, NOW);

        em.clear();

        notificationRepository.markAllAsRead(user.getId());

        assertThat(notificationRepository.countByUserIdAndIsReadFalse(other.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 모두 읽은 상태에서 전체 읽음 처리하면 갱신되는 행이 없다")
    void markAllAsRead_doesNothingWhenNothingUnread() {
        User user = persistUser("owner@example.com");
        Post post = persistPost("국가장학금 1유형");

        persistNotification(user, post, NotificationType.LAST_MINUTE, true, NOW);

        em.clear();

        assertThat(notificationRepository.markAllAsRead(user.getId())).isZero();
    }
}
