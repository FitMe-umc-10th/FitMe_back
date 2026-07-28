package umc.fitme.domain.notify.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RelativeTimeFormatterTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0, 0);

    @Test
    @DisplayName("1분 미만(59초)은 '방금 전'")
    void seconds59_방금전() {
        LocalDateTime createdAt = NOW.minusSeconds(59);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("방금 전");
    }

    @Test
    @DisplayName("정확히 1분은 '1분 전'")
    void minute1_1분전() {
        LocalDateTime createdAt = NOW.minusMinutes(1);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("1분 전");
    }

    @Test
    @DisplayName("59분은 '59분 전'")
    void minutes59_59분전() {
        LocalDateTime createdAt = NOW.minusMinutes(59);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("59분 전");
    }

    @Test
    @DisplayName("정확히 1시간(60분)은 '1시간 전'")
    void hour1_1시간전() {
        LocalDateTime createdAt = NOW.minusHours(1);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("1시간 전");
    }

    @Test
    @DisplayName("23시간은 '23시간 전'")
    void hours23_23시간전() {
        LocalDateTime createdAt = NOW.minusHours(23);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("23시간 전");
    }

    @Test
    @DisplayName("정확히 24시간(1일)은 '어제'")
    void hours24_어제() {
        LocalDateTime createdAt = NOW.minusHours(24);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("어제");
    }

    @Test
    @DisplayName("6일은 '6일 전'")
    void days6_6일전() {
        LocalDateTime createdAt = NOW.minusDays(6);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("6일 전");
    }

    @Test
    @DisplayName("7일은 '1주일 전'")
    void days7_1주일전() {
        LocalDateTime createdAt = NOW.minusDays(7);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("1주일 전");
    }

    @Test
    @DisplayName("4주(28일)는 '4주일 전'")
    void weeks4_4주일전() {
        LocalDateTime createdAt = NOW.minusWeeks(4);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("4주일 전");
    }

    @Test
    @DisplayName("5주(35일)는 'yyyy.MM.dd' 절대 날짜")
    void weeks5_절대날짜() {
        LocalDateTime createdAt = NOW.minusWeeks(5);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("2026.06.15");
    }

    @Test
    @DisplayName("미래 시각은 방어적으로 '방금 전'")
    void future_방금전() {
        LocalDateTime createdAt = NOW.plusMinutes(10);
        assertThat(RelativeTimeFormatter.format(createdAt, NOW)).isEqualTo("방금 전");
    }
}