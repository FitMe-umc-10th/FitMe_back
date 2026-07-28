package umc.fitme.domain.post.sync.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScholarshipOpenApiEndpointResolver {

    private static final String OAS_URL = "https://infuser.odcloud.kr/oas/docs?namespace={publicDataPk}/v1";
    private static final Pattern EIGHT_DIGIT_DATE = Pattern.compile("(\\d{8})");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    private final RestClient restClient;

    @Value("${scholarship.sync.public-data-pk}")
    private String publicDataPk;

    private record OasSpec(Map<String, PathItem> paths) {
    }

    private record PathItem(@JsonProperty("get") Operation operation) {
    }

    private record Operation(String summary) {
    }

    /**
     * 한국장학재단 파일데이터는 재단이 매달 새 파일을 올릴 때마다 새로운 uddi 엔드포인트가
     * 별도로 생성되고 과거 스냅샷은 그대로 남는 구조라, 엔드포인트 하나를 고정해 저장해두면
     * 다음 달부터 동기화가 그 시점에서 멈춘다. 매 실행마다 공개 OAS 문서를 조회해
     * 그 시점의 최신 엔드포인트를 다시 찾는다.
     */
    public String resolveLatestEndpointPath() {
        OasSpec spec = restClient.get()
                .uri(OAS_URL, publicDataPk)
                .retrieve()
                .body(OasSpec.class);

        if (spec == null || spec.paths() == null || spec.paths().isEmpty()) {
            throw new IllegalStateException(
                    "한국장학재단 Open API 명세(OAS)에서 엔드포인트를 찾을 수 없습니다. publicDataPk=" + publicDataPk);
        }

        Map<String, String> summaryByPath = new LinkedHashMap<>();
        spec.paths().forEach((path, item) ->
                summaryByPath.put(path, item.operation() != null ? item.operation().summary() : null));

        String latestPath = pickLatestPath(summaryByPath);
        log.info("한국장학재단 최신 엔드포인트 확인 - path={}", latestPath);
        return latestPath;
    }

    /**
     * summary에 담긴 날짜(예: "..._20260722")를 기준으로 가장 최신 엔드포인트를 고른다.
     * 일부 과거 항목은 날짜 표기가 불규칙해서(예: "10/12/2021") 날짜를 뽑을 수 없는데,
     * 이 경우 OAS 문서에 실린 순서(오래된 것부터 등록됨)를 대신 사용한다.
     */
    static String pickLatestPath(Map<String, String> summaryByPath) {
        String latestPath = null;
        LocalDate latestDate = null;
        int latestIndex = -1;

        int index = 0;
        for (Map.Entry<String, String> entry : summaryByPath.entrySet()) {
            LocalDate date = extractDate(entry.getValue());
            if (latestPath == null || isNewer(date, index, latestDate, latestIndex)) {
                latestPath = entry.getKey();
                latestDate = date;
                latestIndex = index;
            }
            index++;
        }
        return latestPath;
    }

    private static boolean isNewer(LocalDate date, int index, LocalDate latestDate, int latestIndex) {
        if (date != null && latestDate != null) {
            return date.isAfter(latestDate) || (date.equals(latestDate) && index > latestIndex);
        }
        if (date != null) {
            return true;
        }
        if (latestDate != null) {
            return false;
        }
        return index > latestIndex;
    }

    private static LocalDate extractDate(String summary) {
        if (summary == null) {
            return null;
        }
        Matcher matcher = EIGHT_DIGIT_DATE.matcher(summary);
        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group(1);
        }
        if (lastMatch == null) {
            return null;
        }
        try {
            return LocalDate.parse(lastMatch, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
