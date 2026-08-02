package umc.fitme.domain.notify.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import umc.fitme.domain.notify.dto.NotificationResponseDto;
import umc.fitme.domain.notify.entity.Notification;
import umc.fitme.domain.notify.enums.NotificationType;
import umc.fitme.domain.notify.repository.NotificationRepository;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.User;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 31, 12, 0);

    private static final Long USER_ID = 1L;

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-07-31T03:00:00Z"),
                KST
        );
        notificationService = new NotificationService(notificationRepository, fixedClock);
    }

    private User createUser(Long id) {
        return User.builder().id(id).build();
    }

    private Post createPost(Long id) {
        return Post.builder()
                .id(id)
                .postType(PostType.SCHOLARSHIP)
                .title("테스트 공고")
                .organizer("테스트 기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 8, 7))
                .build();
    }

    private Notification createNotification(
            Long id,
            Long userId,
            Long postId,
            boolean isRead,
            LocalDateTime createdAt
    ) {
        return Notification.builder()
                .id(id)
                .user(createUser(userId))
                .post(createPost(postId))
                .title("테스트 공고")
                .message("마감일이 7일 남았습니다. 잊지 말고 지원하세요!")
                .notificationType(NotificationType.LAST_MINUTE)
                .isRead(isRead)
                .createdAt(createdAt)
                .build();
    }

    @Test
    @DisplayName("알림 목록을 조회하면 명세 형식의 응답 항목으로 변환해 내려준다")
    void getNotifications_mapsToResponseItems() {
        Notification notification = createNotification(10L, USER_ID, 100L, false, NOW.minusHours(3));

        given(notificationRepository.findAllByUserIdBeforeCursor(
                eq(USER_ID), anyLong(), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .willReturn(List.of(notification));

        NotificationResponseDto.NotificationListResponse result =
                notificationService.getNotifications(USER_ID, null, 15);

        assertThat(result.notifications()).hasSize(1);
        NotificationResponseDto.NotificationItem item = result.notifications().getFirst();
        assertThat(item.notificationId()).isEqualTo(10L);
        assertThat(item.type()).isEqualTo("DEADLINE");
        assertThat(item.categoryPrefix()).isEqualTo("[마감 임박]");
        assertThat(item.title()).isEqualTo("테스트 공고");
        assertThat(item.message()).isEqualTo("마감일이 7일 남았습니다. 잊지 말고 지원하세요!");
        assertThat(item.postId()).isEqualTo(100L);
        assertThat(item.postType()).isEqualTo("SCHOLARSHIP");
        assertThat(item.isRead()).isFalse();
        assertThat(item.displayTime()).isEqualTo("3시간 전");
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    @DisplayName("요청 개수보다 많이 조회되면 hasNext와 nextCursor를 내려준다")
    void getNotifications_returnsNextCursorWhenMoreExists() {
        Notification first = createNotification(30L, USER_ID, 100L, false, NOW.minusHours(1));
        Notification second = createNotification(20L, USER_ID, 100L, false, NOW.minusHours(2));
        Notification extra = createNotification(10L, USER_ID, 100L, false, NOW.minusHours(3));

        given(notificationRepository.findAllByUserIdBeforeCursor(
                eq(USER_ID), anyLong(), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .willReturn(List.of(first, second, extra));

        NotificationResponseDto.NotificationListResponse result =
                notificationService.getNotifications(USER_ID, null, 2);

        assertThat(result.notifications()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(20L);
    }

    @Test
    @DisplayName("cursor가 없으면 첫 페이지로 조회하고 size는 허용 범위로 보정한다")
    void getNotifications_normalizesCursorAndSize() {
        given(notificationRepository.findAllByUserIdBeforeCursor(
                eq(USER_ID), anyLong(), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .willReturn(List.of());

        notificationService.getNotifications(USER_ID, null, 500);

        ArgumentCaptor<Long> cursorCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findAllByUserIdBeforeCursor(
                eq(USER_ID), cursorCaptor.capture(), pageableCaptor.capture());

        assertThat(cursorCaptor.getValue()).isEqualTo(Long.MAX_VALUE);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(101);
    }

    @Test
    @DisplayName("알림 페이지에 진입해 첫 목록을 받으면 알림을 전부 읽음 처리한다")
    void getNotifications_marksAllAsReadOnFirstPage() {
        Notification notification = createNotification(10L, USER_ID, 100L, false, NOW.minusHours(1));

        given(notificationRepository.findAllByUserIdBeforeCursor(
                eq(USER_ID), anyLong(), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .willReturn(List.of(notification));

        notificationService.getNotifications(USER_ID, null, 15);

        verify(notificationRepository).markAllAsRead(USER_ID);
    }

    @Test
    @DisplayName("커서로 다음 목록을 이어 받을 때는 읽음 처리를 다시 하지 않는다")
    void getNotifications_doesNotMarkAllAsReadOnNextPage() {
        given(notificationRepository.findAllByUserIdBeforeCursor(
                eq(USER_ID), anyLong(), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .willReturn(List.of());

        notificationService.getNotifications(USER_ID, 20L, 15);

        verify(notificationRepository, never()).markAllAsRead(anyLong());
    }

    @Test
    @DisplayName("첫 목록 응답에는 읽음 처리 이전의 상태를 그대로 내려준다")
    void getNotifications_keepsUnreadFlagOnFirstPageResponse() {
        Notification unread = createNotification(10L, USER_ID, 100L, false, NOW.minusHours(1));

        given(notificationRepository.findAllByUserIdBeforeCursor(
                eq(USER_ID), anyLong(), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .willReturn(List.of(unread));

        NotificationResponseDto.NotificationListResponse result =
                notificationService.getNotifications(USER_ID, null, 15);

        assertThat(result.notifications().getFirst().isRead()).isFalse();
        verify(notificationRepository).markAllAsRead(USER_ID);
    }

    @Test
    @DisplayName("미읽음 알림 개수를 조회한다")
    void getUnreadCount_returnsCount() {
        given(notificationRepository.countByUserIdAndIsReadFalse(USER_ID)).willReturn(4L);

        NotificationResponseDto.UnreadCountResponse result = notificationService.getUnreadCount(USER_ID);

        assertThat(result.unreadCount()).isEqualTo(4L);
    }

}
