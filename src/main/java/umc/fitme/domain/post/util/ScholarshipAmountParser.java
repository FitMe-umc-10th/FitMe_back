package umc.fitme.domain.post.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 표시용 문자열 장학금 금액(supportAmount)을 숫자(long)로 변환한다.
 * <p>
 * "최대"/공백/콤마를 제거한 뒤, "숫자+단위"가 실제로 맞붙어 있는 부분만 정규식으로 찾아
 * (숫자 × 단위)로 환산하고 그중 최댓값을 반환한다.
 * <p>
 * 설명 문장에 섞인 연도·날짜처럼 단위가 붙지 않은 숫자는 금액 후보로 잡지 않는다.
 * 금액 후보가 하나도 없을 때는 문자열 전체가 순수 숫자인 경우에만 그 값을 쓰고,
 * 그 외에는 0을 반환한다.
 */
public final class ScholarshipAmountParser {

    /* 단위는 반드시 억 > 천만 > 만 > 천 순으로 시도해야 "천만"이 "천"/"만"보다 먼저 매칭된다 */
    private static final Pattern UNIT_AMOUNT_PATTERN = Pattern.compile("(\\d+)(억|천만|만|천)");
    private static final Pattern WON_AMOUNT_PATTERN = Pattern.compile("(\\d+)원");
    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("\\d+");

    private ScholarshipAmountParser() {
    }

    public static long parse(String raw) {
        if (raw == null) {
            return 0L;
        }

        String normalized = raw.replace("최대", "").replaceAll("[\\s,]", "");

        try {
            boolean found = false;
            long max = 0L;

            // 숫자에 단위가 맞붙은 것만 금액으로 인정 
            Matcher unitMatcher = UNIT_AMOUNT_PATTERN.matcher(normalized);
            while (unitMatcher.find()) {
                long amount = Math.multiplyExact(
                        Long.parseLong(unitMatcher.group(1)), multiplierOf(unitMatcher.group(2)));
                max = Math.max(max, amount);
                found = true;
            }

            // "만원"의 '원'은 앞이 숫자가 아니라 '만'이므로 위 매칭과 중복되지 않는다
            Matcher wonMatcher = WON_AMOUNT_PATTERN.matcher(normalized);
            while (wonMatcher.find()) {
                max = Math.max(max, Long.parseLong(wonMatcher.group(1)));
                found = true;
            }

            if (found) {
                return max;
            }

            // 단위도 '원'도 없지만 문자열 자체가 숫자면 그 값을 금액으로 본다
            if (DIGITS_ONLY_PATTERN.matcher(normalized).matches()) {
                return Long.parseLong(normalized);
            }

            return 0L;
        } catch (NumberFormatException | ArithmeticException e) {
            return 0L;
        }
    }

    private static long multiplierOf(String unit) {
        return switch (unit) {
            case "억" -> 100_000_000L;
            case "천만" -> 10_000_000L;
            case "만" -> 10_000L;
            case "천" -> 1_000L;
            default -> 1L;
        };
    }
}