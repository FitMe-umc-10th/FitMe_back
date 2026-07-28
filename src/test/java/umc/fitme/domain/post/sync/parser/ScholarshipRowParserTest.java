package umc.fitme.domain.post.sync.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import umc.fitme.domain.post.sync.dto.ScholarshipSourceRow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScholarshipRowParserTest {

    private final ScholarshipRowParser parser = new ScholarshipRowParser();

    private Map<String, Object> sampleRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("운영기관명", "한국장학재단");
        row.put("상품명", "국가장학금");
        row.put("상품구분", "장학금");
        row.put("학자금유형구분", "소득연계형");
        row.put("특정자격 상세내용", "대학생");
        row.put("모집시작일", "2026-03-01");
        row.put("모집종료일", "2026-03-31");
        row.put("지원내역 상세내용", "최대 500만원");
        row.put("선발인원 상세내용", "1000");
        return row;
    }

    @Test
    @DisplayName("정상 응답을 파싱하면 필드가 매핑되고 모집시작일~종료일이 하나의 문자열로 합쳐진다")
    void parse_success() {
        List<ScholarshipSourceRow> rows = parser.parse(List.of(sampleRow()));

        assertThat(rows).hasSize(1);
        ScholarshipSourceRow row = rows.get(0);
        assertThat(row.organization()).isEqualTo("한국장학재단");
        assertThat(row.productName()).isEqualTo("국가장학금");
        assertThat(row.productType()).isEqualTo("장학금");
        assertThat(row.supportType()).isEqualTo("소득연계형");
        assertThat(row.applicantTarget()).isEqualTo("대학생");
        assertThat(row.applyPeriodRaw()).isEqualTo("2026-03-01 ~ 2026-03-31");
        assertThat(row.supportAmount()).isEqualTo("최대 500만원");
        assertThat(row.supportCount()).isEqualTo("1000");
    }

    @Test
    @DisplayName("운영기관명이 비어 있는 행은 결과에서 제외된다")
    void parse_skipsRowsWithoutOrganization() {
        Map<String, Object> blankRow = sampleRow();
        blankRow.put("운영기관명", "");

        List<ScholarshipSourceRow> rows = parser.parse(List.of(blankRow, sampleRow()));

        assertThat(rows).hasSize(1);
    }

    @Test
    @DisplayName("필수 필드가 응답에 없으면 예외를 던진다")
    void parse_throws_whenRequiredFieldMissing() {
        Map<String, Object> brokenRow = sampleRow();
        brokenRow.remove("모집종료일");

        assertThatThrownBy(() -> parser.parse(List.of(brokenRow)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("모집종료일");
    }

    @Test
    @DisplayName("빈 목록을 파싱하면 빈 리스트를 반환한다")
    void parse_emptyList() {
        assertThat(parser.parse(List.of())).isEmpty();
    }
}
