package umc.fitme.domain.post.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.repository.ScholarshipRepository;
import umc.fitme.domain.post.sync.dto.ScholarshipCsvRow;
import umc.fitme.domain.post.sync.util.ScholarshipApplyPeriodParser;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipSyncWriter {

    private static final String DEFAULT_APPLICATION_METHOD = "한국장학재단 홈페이지 신청";
    private static final String DEFAULT_APPLICATION_URL = "https://www.kosaf.go.kr";
    private static final String DEFAULT_IMAGE_URL = "https://static.fit-me.site/scholarship-default.png";
    private static final int SOURCE_KEY_CHUNK_SIZE = 500;

    private final ScholarshipRepository scholarshipRepository;
    private final Clock clock;

    public record SyncResult(int insertedCount, int updatedCount, int inactivatedCount, int skippedCount) {
    }

    @Transactional
    public SyncResult applyRows(List<ScholarshipCsvRow> rows) {
        int insertedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        Set<String> sourceKeys = new HashSet<>();
        LocalDateTime now = LocalDateTime.now(clock);

        Map<String, Scholarship> existingBySourceKey = loadExistingBySourceKey(rows);

        for (ScholarshipCsvRow row : rows) {
            try {
                String sourceKey = buildSourceKey(row);
                ScholarshipApplyPeriodParser.ApplyPeriod period =
                        ScholarshipApplyPeriodParser.parse(row.applyPeriodRaw());

                sourceKeys.add(sourceKey);

                Scholarship existing = existingBySourceKey.get(sourceKey);
                if (existing != null) {
                    existing.syncFrom(
                            row.productName(),
                            row.organization(),
                            period.applyStartAt(),
                            period.applyEndAt(),
                            row.applicantTarget(),
                            DEFAULT_APPLICATION_METHOD,
                            DEFAULT_APPLICATION_URL,
                            row.supportAmount(),
                            now
                    );
                    updatedCount++;
                } else {
                    scholarshipRepository.save(toNewScholarship(row, sourceKey, period, now));
                    insertedCount++;
                }
            } catch (Exception e) {
                skippedCount++;
                log.warn("장학금 행 처리 실패 - row={}, reason={}", row, e.getMessage());
            }
        }

        int inactivatedCount = deactivateMissing(sourceKeys, now);

        return new SyncResult(insertedCount, updatedCount, inactivatedCount, skippedCount);
    }

    private Map<String, Scholarship> loadExistingBySourceKey(List<ScholarshipCsvRow> rows) {
        List<String> sourceKeys = rows.stream()
                .map(this::buildSourceKey)
                .distinct()
                .toList();

        Map<String, Scholarship> result = new HashMap<>();
        for (List<String> chunk : partition(sourceKeys, SOURCE_KEY_CHUNK_SIZE)) {
            for (Scholarship scholarship : scholarshipRepository.findAllBySourceKeyIn(chunk)) {
                result.put(scholarship.getSourceKey(), scholarship);
            }
        }
        return result;
    }

    private int deactivateMissing(Set<String> currentSourceKeys, LocalDateTime now) {
        if (currentSourceKeys.isEmpty()) {
            log.warn("이번 동기화에서 유효한 sourceKey가 하나도 없어 비활성화 처리를 건너뜁니다.");
            return 0;
        }

        List<Scholarship> toDeactivate = scholarshipRepository.findAllByActiveTrue().stream()
                .filter(scholarship -> !currentSourceKeys.contains(scholarship.getSourceKey()))
                .toList();
        toDeactivate.forEach(scholarship -> scholarship.deactivate(now));
        return toDeactivate.size();
    }

    private static List<List<String>> partition(List<String> values, int size) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < values.size(); i += size) {
            chunks.add(values.subList(i, Math.min(i + size, values.size())));
        }
        return chunks;
    }

    private String buildSourceKey(ScholarshipCsvRow row) {
        return String.join(
                "|",
                row.organization(),
                row.productName(),
                row.productType(),
                row.applyPeriodRaw()
        );
    }

    private Scholarship toNewScholarship(
            ScholarshipCsvRow row,
            String sourceKey,
            ScholarshipApplyPeriodParser.ApplyPeriod period,
            LocalDateTime now
    ) {
        return Scholarship.builder()
                .postType(PostType.SCHOLARSHIP)
                .title(row.productName())
                .organizer(row.organization())
                .applyStartAt(period.applyStartAt())
                .applyEndAt(period.applyEndAt())
                .summary(row.applicantTarget())
                .applicationMethod(DEFAULT_APPLICATION_METHOD)
                .applicationUrl(DEFAULT_APPLICATION_URL)
                .imageUrl(DEFAULT_IMAGE_URL)
                .createdAt(now)
                .supportAmount(row.supportAmount())
                .sourceKey(sourceKey)
                .active(true)
                .lastSyncedAt(now)
                .build();
    }
}
