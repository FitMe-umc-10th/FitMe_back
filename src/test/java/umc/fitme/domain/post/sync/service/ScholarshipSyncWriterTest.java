package umc.fitme.domain.post.sync.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.repository.ScholarshipRepository;
import umc.fitme.domain.post.sync.dto.ScholarshipSourceRow;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScholarshipSyncWriterTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-07-26T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private ScholarshipRepository scholarshipRepository;

    private ScholarshipSyncWriter scholarshipSyncWriter;

    @BeforeEach
    void setUp() {
        scholarshipSyncWriter = new ScholarshipSyncWriter(scholarshipRepository, FIXED_CLOCK);
    }

    @Test
    @DisplayName("존재하지 않는 sourceKey면 신규 저장하고 insertedCount가 증가한다")
    void applyRows_insertsNew() {
        ScholarshipSourceRow row = new ScholarshipSourceRow(
                "한국장학재단", "신규장학금", "장학금", "성적우수형",
                "대학생", "2026-05-01 ~ 2026-05-31", "최대 300만원", "200"
        );

        given(scholarshipRepository.findAllBySourceKeyIn(anyCollection())).willReturn(List.of());
        given(scholarshipRepository.findAllByActiveTrue()).willReturn(List.of());

        ScholarshipSyncWriter.SyncResult result = scholarshipSyncWriter.applyRows(List.of(row));

        assertThat(result.insertedCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isZero();
        assertThat(result.skippedCount()).isZero();

        ArgumentCaptor<Scholarship> captor = ArgumentCaptor.forClass(Scholarship.class);
        verify(scholarshipRepository).save(captor.capture());

        Scholarship saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("신규장학금");
        assertThat(saved.getOrganizer()).isEqualTo("한국장학재단");
        assertThat(saved.getApplyStartAt()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(saved.getApplyEndAt()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getSourceKey()).isNotBlank();
    }

    @Test
    @DisplayName("기존 sourceKey가 있으면 갱신 처리하고 updatedCount가 증가한다")
    void applyRows_updatesExisting() {
        ScholarshipSourceRow row = new ScholarshipSourceRow(
                "한국장학재단", "국가장학금", "장학금", "소득연계형",
                "대학생", "2026-03-01 ~ 2026-03-31", "최대 500만원", "1000"
        );
        String sourceKey = "한국장학재단|국가장학금|장학금|2026-03-01 ~ 2026-03-31";

        Scholarship existing = Scholarship.builder()
                .postType(PostType.SCHOLARSHIP)
                .title("구 제목")
                .organizer("구 기관")
                .applyStartAt(LocalDate.of(2025, 1, 1))
                .applyEndAt(LocalDate.of(2025, 1, 31))
                .applicationMethod("옛날 방식")
                .applicationUrl("https://old.example.com")
                .imageUrl("https://old.example.com/img.png")
                .createdAt(LocalDateTime.now())
                .sourceKey(sourceKey)
                .active(true)
                .build();

        given(scholarshipRepository.findAllBySourceKeyIn(anyCollection())).willReturn(List.of(existing));
        given(scholarshipRepository.findAllByActiveTrue()).willReturn(List.of());

        ScholarshipSyncWriter.SyncResult result = scholarshipSyncWriter.applyRows(List.of(row));

        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(result.insertedCount()).isZero();

        assertThat(existing.getTitle()).isEqualTo("국가장학금");
        assertThat(existing.getOrganizer()).isEqualTo("한국장학재단");
        assertThat(existing.getSummary()).isEqualTo("대학생");
        assertThat(existing.getSupportAmount()).isEqualTo("최대 500만원");
        assertThat(existing.isActive()).isTrue();

        verify(scholarshipRepository, never()).save(any());
    }

    @Test
    @DisplayName("신청기간 파싱에 실패한 행은 건너뛰고 나머지는 정상 처리한다")
    void applyRows_skipsInvalidRow() {
        ScholarshipSourceRow invalidRow = new ScholarshipSourceRow(
                "기관", "잘못된상품", "장학금", "유형",
                "대상", "형식이상함", "1000만원", "10"
        );
        ScholarshipSourceRow validRow = new ScholarshipSourceRow(
                "한국장학재단", "정상장학금", "장학금", "유형",
                "대상", "2026-06-01 ~ 2026-06-30", "500만원", "50"
        );

        given(scholarshipRepository.findAllBySourceKeyIn(anyCollection())).willReturn(List.of());
        given(scholarshipRepository.findAllByActiveTrue()).willReturn(List.of());

        ScholarshipSyncWriter.SyncResult result =
                scholarshipSyncWriter.applyRows(List.of(invalidRow, validRow));

        assertThat(result.skippedCount()).isEqualTo(1);
        assertThat(result.insertedCount()).isEqualTo(1);
        verify(scholarshipRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("이번 CSV에 없는 기존 active 장학금은 비활성화 처리된다")
    void applyRows_deactivatesMissingScholarships() {
        ScholarshipSourceRow row = new ScholarshipSourceRow(
                "한국장학재단", "국가장학금", "장학금", "유형",
                "대상", "2026-03-01 ~ 2026-03-31", "500만원", "100"
        );

        Scholarship staleScholarship = Scholarship.builder()
                .postType(PostType.SCHOLARSHIP)
                .title("사라진 장학금")
                .organizer("옛 기관")
                .applyStartAt(LocalDate.of(2025, 1, 1))
                .applyEndAt(LocalDate.of(2025, 1, 31))
                .applicationMethod("m")
                .applicationUrl("https://old.example.com")
                .imageUrl("https://old.example.com/img.png")
                .createdAt(LocalDateTime.now())
                .sourceKey("옛 기관|사라진 장학금|장학금|2025-01-01 ~ 2025-01-31")
                .active(true)
                .build();

        given(scholarshipRepository.findAllBySourceKeyIn(anyCollection())).willReturn(List.of());
        given(scholarshipRepository.findAllByActiveTrue()).willReturn(List.of(staleScholarship));

        ScholarshipSyncWriter.SyncResult result = scholarshipSyncWriter.applyRows(List.of(row));

        assertThat(result.inactivatedCount()).isEqualTo(1);
        assertThat(staleScholarship.isActive()).isFalse();
    }

    @Test
    @DisplayName("모든 행이 파싱에 실패하면 기존 active 장학금을 비활성화하지 않는다")
    void applyRows_allRowsInvalid_doesNotDeactivateAnything() {
        ScholarshipSourceRow invalidRow1 = new ScholarshipSourceRow(
                "기관A", "이상한상품A", "장학금", "유형",
                "대상", "형식이상함A", "1000만원", "10"
        );
        ScholarshipSourceRow invalidRow2 = new ScholarshipSourceRow(
                "기관B", "이상한상품B", "장학금", "유형",
                "대상", "형식이상함B", "2000만원", "20"
        );

        given(scholarshipRepository.findAllBySourceKeyIn(anyCollection())).willReturn(List.of());

        ScholarshipSyncWriter.SyncResult result =
                scholarshipSyncWriter.applyRows(List.of(invalidRow1, invalidRow2));

        assertThat(result.skippedCount()).isEqualTo(2);
        assertThat(result.insertedCount()).isZero();
        assertThat(result.updatedCount()).isZero();
        assertThat(result.inactivatedCount()).isZero();

        verify(scholarshipRepository, never()).findAllByActiveTrue();
        verify(scholarshipRepository, never()).save(any());
    }
}
