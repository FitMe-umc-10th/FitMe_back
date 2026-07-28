package umc.fitme.domain.post.sync.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import umc.fitme.domain.post.sync.dto.ScholarshipCsvRow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScholarshipCsvParserTest {

    private final ScholarshipCsvParser parser = new ScholarshipCsvParser();

    @Test
    @DisplayName("정상 CSV를 파싱하면 헤더를 기준으로 각 행이 매핑된다")
    void parse_success() {
        String csv = """
                운영기관명,상품명,상품구분,학자금유형구분,신청대상,신청기간,지원금액,지원인원
                한국장학재단,국가장학금,장학금,소득연계형,대학생,2026-03-01 ~ 2026-03-31,최대 500만원,1000
                """;

        List<ScholarshipCsvRow> rows = parser.parse(csv);

        assertThat(rows).hasSize(1);
        ScholarshipCsvRow row = rows.get(0);
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
    @DisplayName("헤더 순서가 CSV마다 달라도 헤더 이름 기준으로 정확히 매핑된다")
    void parse_success_withDifferentHeaderOrder() {
        String csv = """
                지원인원,지원금액,신청기간,신청대상,학자금유형구분,상품구분,상품명,운영기관명
                500,최대 300만원,2026-04-01 ~ 2026-04-30,대학원생,성적우수형,장학금,대학원장학금,서울시
                """;

        List<ScholarshipCsvRow> rows = parser.parse(csv);

        assertThat(rows).hasSize(1);
        ScholarshipCsvRow row = rows.get(0);
        assertThat(row.organization()).isEqualTo("서울시");
        assertThat(row.productName()).isEqualTo("대학원장학금");
        assertThat(row.supportCount()).isEqualTo("500");
    }

    @Test
    @DisplayName("필수 헤더가 누락되면 실제 헤더 목록을 포함한 예외를 던진다")
    void parse_fail_whenHeaderMissing() {
        String csv = """
                운영기관명,상품명
                한국장학재단,국가장학금
                """;

        assertThatThrownBy(() -> parser.parse(csv))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("상품구분");
    }

    @Test
    @DisplayName("빈 CSV는 빈 리스트를 반환한다")
    void parse_emptyCsv_returnsEmptyList() {
        List<ScholarshipCsvRow> rows = parser.parse("");

        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("첫 컬럼이 비어있는 행은 건너뛴다")
    void parse_skipsBlankRow() {
        String csv = """
                운영기관명,상품명,상품구분,학자금유형구분,신청대상,신청기간,지원금액,지원인원
                한국장학재단,국가장학금,장학금,소득연계형,대학생,2026-03-01 ~ 2026-03-31,최대 500만원,1000
                ,,,,,,,
                """;

        List<ScholarshipCsvRow> rows = parser.parse(csv);

        assertThat(rows).hasSize(1);
    }
}
