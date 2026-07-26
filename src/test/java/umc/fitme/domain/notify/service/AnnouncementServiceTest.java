package umc.fitme.domain.notify.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import umc.fitme.domain.notify.dto.AnnouncementResponseDto;
import umc.fitme.domain.notify.entity.Announcement;
import umc.fitme.domain.notify.enums.AnnouncementCategory;
import umc.fitme.domain.notify.exception.code.AnnouncementErrorCode;
import umc.fitme.domain.notify.repository.AnnouncementRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    // 2026-07-20T12:00 KST
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

    @Mock
    private AnnouncementRepository announcementRepository;

    private AnnouncementService announcementService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-07-20T03:00:00Z"), // = 2026-07-20T12:00 KST
                KST
        );
        announcementService = new AnnouncementService(announcementRepository, fixedClock);
    }

    private Announcement createAnnouncement(
            Long id,
            AnnouncementCategory category,
            String title,
            String content,
            LocalDateTime createdAt
    ) {
        Announcement announcement = Announcement.builder()
                .id(id)
                .announcementCategory(category)
                .title(title)
                .content(content)
                .build();
        ReflectionTestUtils.setField(announcement, "createdAt", createdAt);
        return announcement;
    }

    @Test
    @DisplayName("목록 조회 시 category/categoryName/createdAtString/isNew 가 매핑된다")
    void getAnnouncements_매핑검증() {
        // given
        Announcement recent = createAnnouncement(
                1L, AnnouncementCategory.NOTIFY, "안내 공지", "안내 내용",
                NOW.minusHours(2) // 2시간 전 -> isNew true
        );
        Announcement old = createAnnouncement(
                2L, AnnouncementCategory.INSPECT, "점검 공지", "점검 내용",
                NOW.minusDays(5) // 5일 전 -> isNew false
        );
        given(announcementRepository.findAllByOrderByCreatedAtDesc())
                .willReturn(List.of(recent, old));

        // when
        AnnouncementResponseDto.AnnouncementListResponse response =
                announcementService.getAnnouncements();

        // then
        assertThat(response.announcements()).hasSize(2);

        AnnouncementResponseDto.AnnouncementItem first = response.announcements().get(0);
        assertThat(first.announcementId()).isEqualTo(1L);
        assertThat(first.category()).isEqualTo("NOTIFY");
        assertThat(first.categoryName()).isEqualTo("안내");
        assertThat(first.title()).isEqualTo("안내 공지");
        assertThat(first.createdAtString()).isEqualTo("2시간 전");
        assertThat(first.isNew()).isTrue();

        AnnouncementResponseDto.AnnouncementItem second = response.announcements().get(1);
        assertThat(second.announcementId()).isEqualTo(2L);
        assertThat(second.category()).isEqualTo("INSPECT");
        assertThat(second.categoryName()).isEqualTo("점검");
        assertThat(second.title()).isEqualTo("점검 공지");
        assertThat(second.createdAtString()).isEqualTo("5일 전");
        assertThat(second.isNew()).isFalse();
    }

    @Test
    @DisplayName("상세 조회 성공 시 announcementId/title/content 가 매핑된다")
    void getAnnouncementDetail_성공() {
        // given
        Announcement announcement = createAnnouncement(
                1L, AnnouncementCategory.NOTIFY, "제목", "상세 내용",
                NOW.minusHours(1)
        );
        given(announcementRepository.findById(1L))
                .willReturn(Optional.of(announcement));

        // when
        AnnouncementResponseDto.AnnouncementDetailResponse response =
                announcementService.getAnnouncementDetail(1L);

        // then
        assertThat(response.announcementId()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.content()).isEqualTo("상세 내용");
    }

    @Test
    @DisplayName("상세 조회 실패 시 ANNOUNCEMENT_NOT_FOUND 예외가 발생한다")
    void getAnnouncementDetail_존재하지않음() {
        // given
        given(announcementRepository.findById(999L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> announcementService.getAnnouncementDetail(999L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(AnnouncementErrorCode.ANNOUNCEMENT_NOT_FOUND);
    }
}