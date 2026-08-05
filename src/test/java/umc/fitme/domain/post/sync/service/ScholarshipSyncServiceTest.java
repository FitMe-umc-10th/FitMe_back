package umc.fitme.domain.post.sync.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.post.sync.client.ScholarshipApiClient;
import umc.fitme.domain.post.sync.dto.ScholarshipSourceRow;
import umc.fitme.domain.post.sync.entity.ScholarshipSyncLog;
import umc.fitme.domain.post.sync.enums.ScholarshipSyncStatus;
import umc.fitme.domain.post.sync.parser.ScholarshipRowParser;
import umc.fitme.domain.post.sync.repository.ScholarshipSyncLogRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScholarshipSyncServiceTest {

    @Mock
    private ScholarshipApiClient scholarshipApiClient;
    @Mock
    private ScholarshipRowParser scholarshipRowParser;
    @Mock
    private ScholarshipSyncWriter scholarshipSyncWriter;
    @Mock
    private ScholarshipSyncLogRepository scholarshipSyncLogRepository;

    @InjectMocks
    private ScholarshipSyncService scholarshipSyncService;

    private static final List<Map<String, Object>> SAMPLE_RAW_ROWS = List.of(Map.of("운영기관명", "한국장학재단"));

    private static final ScholarshipSourceRow SAMPLE_ROW = new ScholarshipSourceRow(
            "한국장학재단", "국가장학금", "장학금", "유형",
            "대상", "2026-03-01 ~ 2026-03-31", "500만원", "100"
    );

    @Test
    @DisplayName("정상 동기화 시 성공 로그를 저장한다")
    void sync_success() {
        given(scholarshipApiClient.isConfigured()).willReturn(true);
        given(scholarshipApiClient.fetchAll()).willReturn(SAMPLE_RAW_ROWS);
        given(scholarshipRowParser.parse(SAMPLE_RAW_ROWS)).willReturn(List.of(SAMPLE_ROW));
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
    @DisplayName("응답에 유효한 행이 없으면 writer를 호출하지 않고 실패 로그를 저장한다")
    void sync_emptyRows_skipsWriter() {
        given(scholarshipApiClient.isConfigured()).willReturn(true);
        given(scholarshipApiClient.fetchAll()).willReturn(SAMPLE_RAW_ROWS);
        given(scholarshipRowParser.parse(SAMPLE_RAW_ROWS)).willReturn(List.of());

        scholarshipSyncService.sync();

        verify(scholarshipSyncWriter, never()).applyRows(anyList());

        ArgumentCaptor<ScholarshipSyncLog> captor = ArgumentCaptor.forClass(ScholarshipSyncLog.class);
        verify(scholarshipSyncLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ScholarshipSyncStatus.FAILED);
    }

    @Test
    @DisplayName("Open API 조회 중 예외가 발생하면 실패 로그를 저장한다")
    void sync_fetchFails_savesFailedLog() {
        given(scholarshipApiClient.isConfigured()).willReturn(true);
        given(scholarshipApiClient.fetchAll()).willThrow(new IllegalStateException("조회 실패"));

        scholarshipSyncService.sync();

        ArgumentCaptor<ScholarshipSyncLog> captor = ArgumentCaptor.forClass(ScholarshipSyncLog.class);
        verify(scholarshipSyncLogRepository).save(captor.capture());

        ScholarshipSyncLog savedLog = captor.getValue();
        assertThat(savedLog.getStatus()).isEqualTo(ScholarshipSyncStatus.FAILED);
        assertThat(savedLog.getErrorMessage()).contains("조회 실패");

        verify(scholarshipSyncWriter, never()).applyRows(anyList());
    }

    @Test
    @DisplayName("service-key가 설정되지 않았으면 조회를 시도하지 않고 실패 로그만 저장한다")
    void sync_notConfigured_skipsFetch() {
        given(scholarshipApiClient.isConfigured()).willReturn(false);

        scholarshipSyncService.sync();

        verify(scholarshipApiClient, never()).fetchAll();
        verify(scholarshipSyncWriter, never()).applyRows(anyList());

        ArgumentCaptor<ScholarshipSyncLog> captor = ArgumentCaptor.forClass(ScholarshipSyncLog.class);
        verify(scholarshipSyncLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ScholarshipSyncStatus.FAILED);
    }
}
