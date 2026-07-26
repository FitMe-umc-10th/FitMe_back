package umc.fitme.domain.post.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import umc.fitme.domain.post.sync.client.ScholarshipCsvClient;
import umc.fitme.domain.post.sync.dto.ScholarshipCsvRow;
import umc.fitme.domain.post.sync.entity.ScholarshipSyncLog;
import umc.fitme.domain.post.sync.parser.ScholarshipCsvParser;
import umc.fitme.domain.post.sync.repository.ScholarshipSyncLogRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipSyncService {

    private final ScholarshipCsvClient scholarshipCsvClient;
    private final ScholarshipCsvParser scholarshipCsvParser;
    private final ScholarshipSyncWriter scholarshipSyncWriter;
    private final ScholarshipSyncLogRepository scholarshipSyncLogRepository;

    public void sync() {
        if (!scholarshipCsvClient.isConfigured()) {
            log.warn("scholarship.sync.csv-url 또는 service-key가 설정되지 않아 동기화를 건너뜁니다.");
            scholarshipSyncLogRepository.save(ScholarshipSyncLog.failed("csv-url 또는 service-key 미설정"));
            return;
        }

        try {
            String csv = scholarshipCsvClient.download();
            List<ScholarshipCsvRow> rows = scholarshipCsvParser.parse(csv);

            if (rows.isEmpty()) {
                log.warn("장학금 CSV에 유효한 행이 없어 동기화를 건너뜁니다.");
                scholarshipSyncLogRepository.save(ScholarshipSyncLog.failed("CSV에 유효한 행이 없음"));
                return;
            }

            ScholarshipSyncWriter.SyncResult result = scholarshipSyncWriter.applyRows(rows);

            scholarshipSyncLogRepository.save(ScholarshipSyncLog.success(
                    rows.size(), result.insertedCount(), result.updatedCount(), result.inactivatedCount()
            ));

            log.info(
                    "장학금 동기화 완료 - 전체:{}, 신규:{}, 변경:{}, 비활성화:{}, 실패:{}",
                    rows.size(), result.insertedCount(), result.updatedCount(),
                    result.inactivatedCount(), result.skippedCount()
            );
        } catch (Exception e) {
            log.error("장학금 동기화 실패", e);
            scholarshipSyncLogRepository.save(ScholarshipSyncLog.failed(truncate(e.getMessage())));
        }
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
