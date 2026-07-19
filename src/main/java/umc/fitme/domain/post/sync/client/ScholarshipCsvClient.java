package umc.fitme.domain.post.sync.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScholarshipCsvClient {

    private final RestTemplate restTemplate;

    @Value("${scholarship.sync.csv-url}")
    private String csvUrl;

    @Value("${scholarship.sync.service-key}")
    private String serviceKey;

    @Value("${scholarship.sync.csv-charset:EUC-KR}")
    private String csvCharset;

    public String download() {
        URI uri = UriComponentsBuilder.fromUriString(csvUrl)
                .queryParam("serviceKey", serviceKey)
                .encode()
                .build()
                .toUri();

        ResponseEntity<byte[]> response = restTemplate.getForEntity(uri, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("장학금 CSV 다운로드에 실패했습니다. status=" + response.getStatusCode());
        }

        String content = new String(response.getBody(), Charset.forName(csvCharset));
        log.info("장학금 CSV 다운로드 완료 - byteSize={}", response.getBody().length);

        return content;
    }
}
