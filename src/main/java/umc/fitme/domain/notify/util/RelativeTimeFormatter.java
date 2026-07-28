package umc.fitme.domain.notify.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class RelativeTimeFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private RelativeTimeFormatter() {
    }

    public static String format(LocalDateTime createdAt, LocalDateTime now) {
        long seconds = ChronoUnit.SECONDS.between(createdAt, now);

        // 미래 시각 방어 처리
        if (seconds < 60) {
            return "방금 전";
        }

        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = ChronoUnit.HOURS.between(createdAt, now);
        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = ChronoUnit.DAYS.between(createdAt, now);
        if (days == 1) {
            return "어제";
        }
        if (days < 7) {
            return days + "일 전";
        }

        long weeks = days / 7;
        if (weeks < 5) {
            return weeks + "주일 전";
        }

        return createdAt.format(DATE_FORMATTER);
    }
}