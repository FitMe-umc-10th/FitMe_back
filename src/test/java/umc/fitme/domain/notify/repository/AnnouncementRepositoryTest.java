package umc.fitme.domain.notify.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;
import umc.fitme.domain.notify.entity.Announcement;
import umc.fitme.domain.notify.enums.AnnouncementCategory;
import umc.fitme.support.RepositoryTestSupport;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnnouncementRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private TestEntityManager em;

    /** 고정 기준 시각 (테스트 결정성을 위해 현재 시각 대신 사용) */
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0, 0);

    /**
     * announcement 를 persist 한 뒤, @CreatedDate 오토세팅 값을 덮어써서 createdAt 을 명시 주입한다.
     * (AnnouncementServiceTest 의 createAnnouncement 헬퍼와 동일한 createdAt 주입 방식)
     */
    private void persistWithCreatedAt(
            AnnouncementCategory category,
            String title,
            String content,
            LocalDateTime createdAt
    ) {
        Announcement announcement = Announcement.builder()
                .announcementCategory(category)
                .title(title)
                .content(content)
                .build();
        em.persist(announcement);
        ReflectionTestUtils.setField(announcement, "createdAt", createdAt);
    }

    @Test
    @DisplayName("findAllByOrderByCreatedAtDesc 는 createdAt 내림차순(최신순)으로 반환한다")
    void findAllByOrderByCreatedAtDesc_최신순() {
        // given: 저장 순서를 정렬 순서와 다르게 넣는다
        persistWithCreatedAt(AnnouncementCategory.NOTIFY, "2일 전", "내용 A", NOW.minusDays(2));
        persistWithCreatedAt(AnnouncementCategory.INSPECT, "방금", "내용 B", NOW.minusMinutes(1));
        persistWithCreatedAt(AnnouncementCategory.NOTIFY, "5일 전", "내용 C", NOW.minusDays(5));
        em.flush();
        em.clear();

        // when
        List<Announcement> result = announcementRepository.findAllByOrderByCreatedAtDesc();

        // then: title 이 최신순으로 정렬된다
        assertThat(result)
                .extracting(Announcement::getTitle)
                .containsExactly("방금", "2일 전", "5일 전");

        // then: 인접 원소 간 createdAt 이 내림차순(앞이 뒤보다 같거나 이후)이다
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getCreatedAt())
                    .isAfterOrEqualTo(result.get(i + 1).getCreatedAt());
        }
    }
}