package umc.fitme.domain.post.sync.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.post.sync.client.ScholarshipCsvClient;
import umc.fitme.domain.post.sync.dto.ScholarshipCsvRow;
import umc.fitme.domain.post.sync.entity.ScholarshipSyncLog;
import umc.fitme.domain.post.sync.enums.ScholarshipSyncStatus;
import umc.fitme.domain.post.sync.parser.ScholarshipCsvParser;
import umc.fitme.domain.post.sync.repository.ScholarshipSyncLogRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScholarshipSyncServiceTest {

    @Mock
    private ScholarshipCsvClient scholarshipCsvClient;
    @Mock
    private ScholarshipCsvParser scholarshipCsvParser;
    @Mock
    private ScholarshipSyncWriter scholarshipSyncWriter;
    @Mock
    private ScholarshipSyncLogRepository scholarshipSyncLogRepository;

    @InjectMocks
    private ScholarshipSyncService scholarshipSyncService;

    private static final ScholarshipCsvRow SAMPLE_ROW = new ScholarshipCsvRow(
            "한국장학재단", "국가장학금", "장학금", "유형",
            "대상", "2026-03-01 ~ 2026-03-31", "500만원", "100"
    );

    @Test
    @DisplayName("정상 동기화 시 성공 로그를 저장한다")
    void sync_success() {
        given(scholarshipCsvClient.download()).willReturn("csv-content");
        given(scholarshipCsvParser.parse("csv-content")).willReturn(List.of(SAMPLE_ROW));
        given(scholarshipSyncWriter.applyRows(List.of(SAMPLE_ROW)))
                .willReturn(new ScholarshipSyncWriter.SyncResult(1, 2, 3, 0));

        scholarshipSyncService.sync();

        ArgumentCaptor<ScholarshipSyncLog> captor = ArgumentCaptor.forClass(ScholarshipSyncLog.class);
        verify(scholarshipSyncLogRepository).save(captor.capture());

        ScholarshipSyncLog savedLog = captor.getValue();
        assertThat(savedLog.getStatus()).isEqualTo(ScholarshipSyncStatus.SUCCESS);
        assertThat(savedLog.getTotalCount()).isEqualTo(1);
        assertThat(savedLog.getInsertedCount()).isEqualTo(1);
        assertThat(savedLog.getUpdatedCount()).isEqualTo(2);
        assertThat(savedLog.getInactivatedCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("CSV에 유효한 행이 없으면 writer를 호출하지 않고 실패 로그를 저장한다")
    void sync_emptyRows_skipsWriter() {
        given(scholarshipCsvClient.download()).willReturn("csv-content");
        given(scholarshipCsvParser.parse("csv-content")).willReturn(List.of());

        scholarshipSyncService.sync();

        verify(scholarshipSyncWriter, never()).applyRows(anyList());

        ArgumentCaptor<ScholarshipSyncLog> captor = ArgumentCaptor.forClass(ScholarshipSyncLog.class);
        verify(scholarshipSyncLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ScholarshipSyncStatus.FAILED);
    }

    @Test
    @DisplayName("CSV 다운로드 중 예외가 발생하면 실패 로그를 저장한다")
    void sync_downloadFails_savesFailedLog() {
        given(scholarshipCsvClient.download()).willThrow(new IllegalStateException("다운로드 실패"));

        scholarshipSyncService.sync();

        ArgumentCaptor<ScholarshipSyncLog> captor = ArgumentCaptor.forClass(ScholarshipSyncLog.class);
        verify(scholarshipSyncLogRepository).save(captor.capture());

        ScholarshipSyncLog savedLog = captor.getValue();
        assertThat(savedLog.getStatus()).isEqualTo(ScholarshipSyncStatus.FAILED);
        assertThat(savedLog.getErrorMessage()).contains("다운로드 실패");

        verify(scholarshipSyncWriter, never()).applyRows(anyList());
    }
}
