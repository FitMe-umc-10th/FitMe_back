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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipSyncWriter {

    private static final String DEFAULT_APPLICATION_METHOD = "한국장학재단 홈페이지 신청";
    private static final String DEFAULT_APPLICATION_URL = "https://www.kosaf.go.kr";
    private static final String DEFAULT_IMAGE_URL = "https://static.fit-me.site/scholarship-default.png";

    private final ScholarshipRepository scholarshipRepository;

    public record SyncResult(int insertedCount, int updatedCount, int inactivatedCount, int skippedCount) {
    }

    @Transactional
    public SyncResult applyRows(List<ScholarshipCsvRow> rows) {
        int insertedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        Set<String> sourceKeys = new HashSet<>();

        for (ScholarshipCsvRow row : rows) {
            try {
                String sourceKey = buildSourceKey(row);
                ScholarshipApplyPeriodParser.ApplyPeriod period =
                        ScholarshipApplyPeriodParser.parse(row.applyPeriodRaw());

                sourceKeys.add(sourceKey);

                Optional<Scholarship> existing = scholarshipRepository.findBySourceKey(sourceKey);
                if (existing.isPresent()) {
                    existing.get().syncFrom(
                            row.productName(),
                            row.organization(),
                            period.applyStartAt(),
                            period.applyEndAt(),
                            row.applicantTarget(),
                            DEFAULT_APPLICATION_METHOD,
                            DEFAULT_APPLICATION_URL,
                            row.supportAmount()
                    );
                    updatedCount++;
                } else {
                    scholarshipRepository.save(toNewScholarship(row, sourceKey, period));
                    insertedCount++;
                }
            } catch (Exception e) {
                skippedCount++;
                log.warn("장학금 행 처리 실패 - row={}, reason={}", row, e.getMessage());
            }
        }

        int inactivatedCount = deactivateMissing(sourceKeys);

        return new SyncResult(insertedCount, updatedCount, inactivatedCount, skippedCount);
    }

    private int deactivateMissing(Set<String> currentSourceKeys) {
        List<Scholarship> toDeactivate = scholarshipRepository.findAllByActiveTrueAndSourceKeyNotIn(currentSourceKeys);
        toDeactivate.forEach(Scholarship::deactivate);
        return toDeactivate.size();
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
            ScholarshipApplyPeriodParser.ApplyPeriod period
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
                .createdAt(LocalDateTime.now())
                .supportAmount(row.supportAmount())
                .sourceKey(sourceKey)
                .active(true)
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }
}
