package umc.fitme.domain.post.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import umc.fitme.domain.post.dto.publicapi.PublicApiResponse;
import java.net.URI;

@Component
@RequiredArgsConstructor
public class PublicDataApiClient {

    private final WebClient webClient;

    @Value("${public-data.api-key}")
    private String serviceKey;

    @Value("${public-data.url}")
    private String apiUrl;

    public PublicApiResponse fetchScholarshipData(int page, int perPage) {


        String requestUrl = apiUrl + "?page=" + page + "&perPage=" + perPage + "&serviceKey=" + serviceKey;

        return webClient.get()
                .uri(URI.create(requestUrl))
                .retrieve()
                .bodyToMono(PublicApiResponse.class)
                .block();
    }
}