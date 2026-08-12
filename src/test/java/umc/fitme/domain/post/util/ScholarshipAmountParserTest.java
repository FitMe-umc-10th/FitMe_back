package umc.fitme.domain.post.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

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

    /* 실제 공고 원문처럼 금액이 여러 개거나 연도·날짜가 섞인 설명 문장 */
    static Stream<Arguments> 설명_문장이_섞인_원문() {
        return Stream.of(
                // 금액이 여러 개면 최댓값을 쓴다
                Arguments.of("○ 총 1200만원※ 1년간 지원 (학기당 600만원)※ 정규학기 동안만 지원", 12_000_000L),
                Arguments.of("○ A학점 이상 각 300만원○ B학점 이상 각 250만원", 3_000_000L),
                // 단위가 붙지 않은 연도(2025)는 금액으로 세지 않는다
                Arguments.of("○ 2025년 300만원 지원", 3_000_000L),
                // 단위가 붙은 숫자가 하나도 없는 설명 문장은 0
                Arguments.of("○ 한국장학재단으로부터 실행한 학자금 대출 중 ′25년도 하반기와 '26년도 상반기"
                        + "(′25. 7. 1.~′26. 6. 30.) 동안의 발생 이자", 0L)
        );
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @MethodSource("설명_문장이_섞인_원문")
    void parse_단위가_붙은_금액만_인식하고_최댓값을_반환한다(String input, long expected) {
        assertThat(ScholarshipAmountParser.parse(input)).isEqualTo(expected);
    }
}