package umc.fitme.domain.post.sync.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import umc.fitme.domain.post.sync.dto.ScholarshipSourceRow;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ScholarshipRowParser {

    private static final String FIELD_ORGANIZATION = "운영기관명";
    private static final String FIELD_PRODUCT_NAME = "상품명";
    private static final String FIELD_PRODUCT_TYPE = "상품구분";
    private static final String FIELD_SUPPORT_TYPE = "학자금유형구분";
    private static final String FIELD_APPLICANT_TARGET = "특정자격 상세내용";
    private static final String FIELD_RECRUIT_START = "모집시작일";
    private static final String FIELD_RECRUIT_END = "모집종료일";
    private static final String FIELD_SUPPORT_AMOUNT = "지원내역 상세내용";
    private static final String FIELD_SUPPORT_COUNT = "선발인원 상세내용";

    private static final List<String> REQUIRED_FIELDS = List.of(
            FIELD_ORGANIZATION, FIELD_PRODUCT_NAME, FIELD_PRODUCT_TYPE,
            FIELD_SUPPORT_TYPE, FIELD_RECRUIT_START, FIELD_RECRUIT_END
    );

    public List<ScholarshipSourceRow> parse(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        validateSchema(rows.get(0));

        return rows.stream()
                .filter(row -> hasText(stringOf(row, FIELD_ORGANIZATION)))
                .map(this::toRow)
                .toList();
    }

    private void validateSchema(Map<String, Object> sampleRow) {
        List<String> missing = REQUIRED_FIELDS.stream()
                .filter(field -> !sampleRow.containsKey(field))
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "한국장학재단 Open API 응답에서 다음 필드를 찾을 수 없습니다: " + missing
                            + " / 실제 필드 목록: " + sampleRow.keySet()
                            + " -> ScholarshipRowParser의 FIELD_* 상수를 실제 응답에 맞게 수정하세요."
            );
        }
    }

    private ScholarshipSourceRow toRow(Map<String, Object> row) {
        String applyPeriodRaw = stringOf(row, FIELD_RECRUIT_START) + " ~ " + stringOf(row, FIELD_RECRUIT_END);

        return new ScholarshipSourceRow(
                stringOf(row, FIELD_ORGANIZATION),
                stringOf(row, FIELD_PRODUCT_NAME),
                stringOf(row, FIELD_PRODUCT_TYPE),
                stringOf(row, FIELD_SUPPORT_TYPE),
                stringOf(row, FIELD_APPLICANT_TARGET),
                applyPeriodRaw,
                stringOf(row, FIELD_SUPPORT_AMOUNT),
                stringOf(row, FIELD_SUPPORT_COUNT)
        );
    }

    private String stringOf(Map<String, Object> row, String field) {
        Object value = row.get(field);
        return value == null ? "" : value.toString().trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
