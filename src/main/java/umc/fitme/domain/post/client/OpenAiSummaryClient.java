package umc.fitme.domain.post.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiSummaryClient {

    private static final String SYSTEM_PROMPT =
            "너는 대학생 대상 장학금/공모전 공고를 한국어로 간결하게 요약하는 도우미야. " +
                    "3~4문장 이내로, 신청 대상과 핵심 조건 위주로 요약해줘.";
    private static final int MAX_TOKENS = 300;
    private static final double TEMPERATURE = 0.5;

    private final RestClient restClient;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.api-url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    /**
     * API 키가 배포 환경변수에 설정되지 않은 경우(빈 문자열)를 판별한다.
     * 값이 없을 때 generateSummary()를 호출하면 인증 실패 요청을 반복하게 되므로,
     * 호출 전에 이 메서드로 먼저 확인해 생성을 건너뛰어야 한다.
     */
    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    public String generateSummary(String prompt) {
        ChatRequest request = new ChatRequest(
                model,
                List.of(
                        new ChatMessage("system", SYSTEM_PROMPT),
                        new ChatMessage("user", prompt)
                ),
                MAX_TOKENS,
                TEMPERATURE
        );

        ChatResponse response = restClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }

        return response.choices().get(0).message().content().trim();
    }

    private record ChatMessage(String role, String content) {
    }

    private record ChatRequest(
            String model,
            List<ChatMessage> messages,
            @JsonProperty("max_tokens") int maxTokens,
            double temperature
    ) {
    }

    private record ChatResponse(List<Choice> choices) {
    }

    private record Choice(ChatMessage message) {
    }
}
