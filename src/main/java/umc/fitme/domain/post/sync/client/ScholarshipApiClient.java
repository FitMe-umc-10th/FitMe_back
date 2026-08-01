package umc.fitme.domain.post.sync.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScholarshipApiClient {

    private static final int PAGE_SIZE = 1000;
    private static final String DATA_BASE_URL = "https://api.odcloud.kr/api";

    private final RestClient restClient;
    private final ScholarshipOpenApiEndpointResolver endpointResolver;

    @Value("${scholarship.sync.service-key}")
    private String serviceKey;

    private record OdcloudResponse(
            int totalCount,
            int currentCount,
            List<Map<String, Object>> data
    ) {
    }

    /**
     * service-key가 배포 환경변수에 설정되지 않은 경우(빈 문자열)를 판별한다.
     * 값이 없을 때 fetchAll()을 호출하면 인증키 없이 요청을 시도하게 되므로,
     * 호출 전에 이 메서드로 먼저 확인해 동기화를 건너뛰어야 한다.
     */
    public boolean isConfigured() {
        return StringUtils.hasText(serviceKey);
    }

    /**
     * odcloud Open API는 페이지당 조회 가능한 건수 제한이 있어(perPage), 전체 데이터를 받으려면
     * totalCount만큼 채워질 때까지 페이지를 넘기며 반복 호출해야 한다.
     */
    public List<Map<String, Object>> fetchAll() {
        String endpointPath = endpointResolver.resolveLatestEndpointPath();

        List<Map<String, Object>> allRows = new ArrayList<>();
        int page = 1;
        while (true) {
            OdcloudResponse response = restClient.get()
                    .uri(buildUri(endpointPath, page))
                    .retrieve()
                    .body(OdcloudResponse.class);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                break;
            }

            allRows.addAll(response.data());

            if (allRows.size() >= response.totalCount() || response.currentCount() < PAGE_SIZE) {
                break;
            }
            page++;
        }

        log.info("장학금 Open API 조회 완료 - endpoint={}, rowCount={}", endpointPath, allRows.size());
        return allRows;
    }

    private URI buildUri(String endpointPath, int page) {
        // 공공데이터포털 인증키는 이미 URL 인코딩된 상태로 발급되는 경우가 많아,
        // encode()를 다시 태우면 '%'가 이중 인코딩되어 인증키가 깨진다. build(true)로 그대로 사용한다.
        return UriComponentsBuilder.fromUriString(DATA_BASE_URL + endpointPath)
                .queryParam("page", page)
                .queryParam("perPage", PAGE_SIZE)
                .queryParam("returnType", "JSON")
                .queryParam("serviceKey", serviceKey)
                .build(true)
                .toUri();
    }
}
