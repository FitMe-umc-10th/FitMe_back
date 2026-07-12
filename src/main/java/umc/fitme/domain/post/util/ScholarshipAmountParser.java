package umc.fitme.domain.post.util;

/**
 * 표시용 문자열 장학금 금액(supportAmount)을 숫자(long)로 변환한다.
 * <p>
 * "최대"/공백/콤마를 제거한 뒤 단위를 판별해 (앞의 숫자 × 단위)로 계산한다.
 * 단위 판별 우선순위: 억 &gt; 천만 &gt; 만 &gt; 천 (없으면 원).
 * 숫자가 없거나 파싱에 실패하면 0을 반환한다.
 */
public final class ScholarshipAmountParser {

    private ScholarshipAmountParser() {
    }

    public static long parse(String raw) {
        if (raw == null) {
            return 0L;
        }

        String normalized = raw.replace("최대", "").replaceAll("[\\s,]", "");

        long multiplier;
        String unit;
        if (normalized.contains("억")) {
            multiplier = 100_000_000L;
            unit = "억";
        } else if (normalized.contains("천만")) {
            multiplier = 10_000_000L;
            unit = "천만";
        } else if (normalized.contains("만")) {
            multiplier = 10_000L;
            unit = "만";
        } else if (normalized.contains("천")) {
            multiplier = 1_000L;
            unit = "천";
        } else {
            multiplier = 1L;
            unit = null;
        }

        String source = (unit == null)
                ? normalized
                : normalized.substring(0, normalized.indexOf(unit));
        String digits = source.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0L;
        }

        try {
            return Long.parseLong(digits) * multiplier;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}