package umc.fitme.domain.post.sync.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScholarshipCsvClient {

    private final RestClient restClient;

    @Value("${scholarship.sync.csv-url}")
    private String csvUrl;

    @Value("${scholarship.sync.service-key}")
    private String serviceKey;

    @Value("${scholarship.sync.csv-charset:EUC-KR}")
    private String csvCharset;

    /**
     * csv-url / service-key가 배포 환경변수에 설정되지 않은 경우(빈 문자열)를 판별한다.
     * 값이 없을 때 download()를 호출하면 잘못된 URL로 요청을 시도하게 되므로,
     * 호출 전에 이 메서드로 먼저 확인해 동기화를 건너뛰어야 한다.
     */
    public boolean isConfigured() {
        return StringUtils.hasText(csvUrl) && StringUtils.hasText(serviceKey);
    }

    public String download() {
        // 공공데이터포털 인증키는 이미 URL 인코딩된 상태로 발급되는 경우가 많아,
        // encode()를 다시 태우면 '%'가 이중 인코딩되어 인증키가 깨진다. build(true)로 그대로 사용한다.
        URI uri = UriComponentsBuilder.fromUriString(csvUrl)
                .queryParam("serviceKey", serviceKey)
                .build(true)
                .toUri();

        byte[] body = restClient.get()
                .uri(uri)
                .retrieve()
                .body(byte[].class);

        if (body == null) {
            throw new IllegalStateException("장학금 CSV 다운로드에 실패했습니다. 응답 본문이 비어 있습니다.");
        }

        String content = new String(body, Charset.forName(csvCharset));
        log.info("장학금 CSV 다운로드 완료 - byteSize={}", body.length);

        return content;
    }
}
