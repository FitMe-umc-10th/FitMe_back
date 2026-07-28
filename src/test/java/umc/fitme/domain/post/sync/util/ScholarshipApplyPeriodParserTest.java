package umc.fitme.domain.post.sync.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import umc.fitme.domain.post.sync.util.ScholarshipApplyPeriodParser.ApplyPeriod;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScholarshipApplyPeriodParserTest {

    @Test
    @DisplayName("yyyy-MM-dd ~ yyyy-MM-dd 형식을 파싱한다")
    void parse_dashFormat_withTilde() {
        ApplyPeriod period = ScholarshipApplyPeriodParser.parse("2026-03-01 ~ 2026-03-31");

        assertThat(period.applyStartAt()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(period.applyEndAt()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    @DisplayName("공백 없는 yyyy.MM.dd~yyyy.MM.dd 형식도 파싱한다")
    void parse_dotFormat_withoutSpaces() {
        ApplyPeriod period = ScholarshipApplyPeriodParser.parse("2026.04.01~2026.04.30");

        assertThat(period.applyStartAt()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(period.applyEndAt()).isEqualTo(LocalDate.of(2026, 4, 30));
    }

    @Test
    @DisplayName("' - ' 구분자 형식도 파싱한다")
    void parse_dashDelimiter() {
        ApplyPeriod period = ScholarshipApplyPeriodParser.parse("2026-05-01 - 2026-05-31");

        assertThat(period.applyStartAt()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(period.applyEndAt()).isEqualTo(LocalDate.of(2026, 5, 31));
    }

    @Test
    @DisplayName("null 또는 빈 값이면 예외를 던진다")
    void parse_blank_throwsException() {
        assertThatThrownBy(() -> ScholarshipApplyPeriodParser.parse(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ScholarshipApplyPeriodParser.parse("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("구분자가 없어 시작/종료일을 나눌 수 없으면 예외를 던진다")
    void parse_noDelimiter_throwsException() {
        assertThatThrownBy(() -> ScholarshipApplyPeriodParser.parse("2026-03-01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("신청기간 형식");
    }

    @Test
    @DisplayName("날짜 포맷을 인식할 수 없으면 예외를 던진다")
    void parse_unrecognizedDateFormat_throwsException() {
        assertThatThrownBy(() -> ScholarshipApplyPeriodParser.parse("모집중 ~ 마감시까지"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 날짜(2월 30일 등)면 예외를 던진다")
    void parse_invalidCalendarDate_throwsException() {
        assertThatThrownBy(() -> ScholarshipApplyPeriodParser.parse("2026-02-30 ~ 2026-03-05"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
