package umc.fitme.domain.user.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MyPageServiceTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "5000000, 5000000",
            "'최대 1,000,000원', 1000000",
            "'1,000,000', 1000000",
            "'250만원', 2500000",
            "'최대 250만원', 2500000",
            "'1,000만원', 10000000"
    })
    void parseAmount_숫자와_콤마_표기를_long으로_변환한다(String input, long expected) {
        assertThat(MyPageService.parseAmount(input)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "\"{0}\" -> 0")
    @NullSource
    @ValueSource(strings = {"", "전액", "미정"})
    void parseAmount_숫자가_없거나_null이면_0을_반환한다(String input) {
        assertThat(MyPageService.parseAmount(input)).isEqualTo(0L);
    }
}