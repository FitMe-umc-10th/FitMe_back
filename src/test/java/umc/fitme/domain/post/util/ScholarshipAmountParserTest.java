package umc.fitme.domain.post.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ScholarshipAmountParserTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "'2,500,000원', 2500000",
            "'250만원', 2500000",
            "'3천만원', 30000000",
            "'5천원', 5000",
            "'1억원', 100000000",
            "5000000, 5000000",
            "'최대 1,000,000원', 1000000",
            "'최대 250만원', 2500000",
            "'1,000만원', 10000000"
    })
    void parse_단위를_판별해_숫자로_변환한다(String input, long expected) {
        assertThat(ScholarshipAmountParser.parse(input)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "\"{0}\" -> 0")
    @NullSource
    @ValueSource(strings = {"", "전액", "미정"})
    void parse_숫자가_없거나_null이면_0을_반환한다(String input) {
        assertThat(ScholarshipAmountParser.parse(input)).isZero();
    }
}