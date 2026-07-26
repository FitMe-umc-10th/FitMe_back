package umc.fitme.domain.post.sync.parser;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import umc.fitme.domain.post.sync.dto.ScholarshipCsvRow;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ScholarshipCsvParser {

    private static final String HEADER_ORGANIZATION = "운영기관명";
    private static final String HEADER_PRODUCT_NAME = "상품명";
    private static final String HEADER_PRODUCT_TYPE = "상품구분";
    private static final String HEADER_SUPPORT_TYPE = "학자금유형구분";
    private static final String HEADER_APPLICANT_TARGET = "신청대상";
    private static final String HEADER_APPLY_PERIOD = "신청기간";
    private static final String HEADER_SUPPORT_AMOUNT = "지원금액";
    private static final String HEADER_SUPPORT_COUNT = "지원인원";

    public List<ScholarshipCsvRow> parse(String csvContent) {
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(csvContent)).build()) {
            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) {
                return List.of();
            }

            Map<String, Integer> headerIndex = resolveHeaderIndex(allRows.get(0));
            List<String[]> dataRows = allRows.subList(1, allRows.size());

            return dataRows.stream()
                    .filter(row -> row.length > 0 && hasText(row[0]))
                    .map(row -> toRow(row, headerIndex))
                    .toList();
        } catch (IOException | CsvException e) {
            throw new IllegalStateException("장학금 CSV 파싱에 실패했습니다.", e);
        }
    }

    private Map<String, Integer> resolveHeaderIndex(String[] headerRow) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headerRow.length; i++) {
            index.put(headerRow[i].trim(), i);
        }

        List<String> requiredHeaders = List.of(
                HEADER_ORGANIZATION, HEADER_PRODUCT_NAME, HEADER_PRODUCT_TYPE,
                HEADER_SUPPORT_TYPE, HEADER_APPLICANT_TARGET, HEADER_APPLY_PERIOD,
                HEADER_SUPPORT_AMOUNT, HEADER_SUPPORT_COUNT
        );

        List<String> missing = requiredHeaders.stream()
                .filter(header -> !index.containsKey(header))
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "CSV 헤더에서 다음 컬럼을 찾을 수 없습니다: " + missing
                            + " / 실제 헤더 목록: " + index.keySet()
                            + " -> ScholarshipCsvParser의 HEADER_* 상수를 실제 파일에 맞게 수정하세요."
            );
        }

        return index;
    }

    private ScholarshipCsvRow toRow(String[] columns, Map<String, Integer> headerIndex) {
        return new ScholarshipCsvRow(
                valueOf(columns, headerIndex, HEADER_ORGANIZATION),
                valueOf(columns, headerIndex, HEADER_PRODUCT_NAME),
                valueOf(columns, headerIndex, HEADER_PRODUCT_TYPE),
                valueOf(columns, headerIndex, HEADER_SUPPORT_TYPE),
                valueOf(columns, headerIndex, HEADER_APPLICANT_TARGET),
                valueOf(columns, headerIndex, HEADER_APPLY_PERIOD),
                valueOf(columns, headerIndex, HEADER_SUPPORT_AMOUNT),
                valueOf(columns, headerIndex, HEADER_SUPPORT_COUNT)
        );
    }

    private String valueOf(String[] columns, Map<String, Integer> headerIndex, String headerName) {
        int idx = headerIndex.get(headerName);
        if (idx >= columns.length) {
            return "";
        }
        return columns[idx] == null ? "" : columns[idx].trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
