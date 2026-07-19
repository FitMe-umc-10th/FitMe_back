package umc.fitme.domain.post.sync.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public final class ScholarshipApplyPeriodParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd")
    );

    private ScholarshipApplyPeriodParser() {
    }

    public record ApplyPeriod(LocalDate applyStartAt, LocalDate applyEndAt) {
    }

    public static ApplyPeriod parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("신청기간 값이 비어 있습니다.");
        }

        String[] cleaned = normalize(raw);

        if (cleaned.length != 2) {
            throw new IllegalArgumentException("신청기간 형식을 파싱할 수 없습니다: " + raw);
        }

        LocalDate start = parseDate(cleaned[0])
                .orElseThrow(() -> new IllegalArgumentException("신청 시작일을 파싱할 수 없습니다: " + cleaned[0]));
        LocalDate end = parseDate(cleaned[1])
                .orElseThrow(() -> new IllegalArgumentException("신청 종료일을 파싱할 수 없습니다: " + cleaned[1]));

        return new ApplyPeriod(start, end);
    }

    private static String[] normalize(String raw) {
        String trimmed = raw.trim();
        // "~" 구분자를 우선 시도하고, 없으면 " - " 구분자를 시도한다.
        if (trimmed.contains("~")) {
            return splitAndTrim(trimmed, "~");
        }
        if (trimmed.contains(" - ")) {
            return splitAndTrim(trimmed, " - ");
        }
        return new String[]{trimmed};
    }

    private static String[] splitAndTrim(String value, String delimiter) {
        String[] parts = value.split(java.util.regex.Pattern.quote(delimiter), 2);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    private static Optional<LocalDate> parseDate(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return Optional.of(LocalDate.parse(value, formatter));
            } catch (Exception ignored) {
                // 다음 포맷 시도
            }
        }
        return Optional.empty();
    }
}
