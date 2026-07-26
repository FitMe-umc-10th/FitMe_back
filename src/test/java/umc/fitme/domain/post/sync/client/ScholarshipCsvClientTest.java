package umc.fitme.domain.post.sync.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ScholarshipCsvClientTest {

    private ScholarshipCsvClient newClient(String csvUrl, String serviceKey) {
        ScholarshipCsvClient client = new ScholarshipCsvClient(mock(RestClient.class));
        ReflectionTestUtils.setField(client, "csvUrl", csvUrl);
        ReflectionTestUtils.setField(client, "serviceKey", serviceKey);
        return client;
    }

    @Test
    @DisplayName("csv-url과 service-key가 모두 있으면 설정된 것으로 본다")
    void isConfigured_true_whenBothPresent() {
        ScholarshipCsvClient client = newClient("https://example.com/csv", "service-key");

        assertThat(client.isConfigured()).isTrue();
    }

    @Test
    @DisplayName("csv-url이 비어 있으면 설정되지 않은 것으로 본다")
    void isConfigured_false_whenCsvUrlBlank() {
        ScholarshipCsvClient client = newClient("", "service-key");

        assertThat(client.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("service-key가 비어 있으면 설정되지 않은 것으로 본다")
    void isConfigured_false_whenServiceKeyBlank() {
        ScholarshipCsvClient client = newClient("https://example.com/csv", "");

        assertThat(client.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("둘 다 비어 있으면 설정되지 않은 것으로 본다")
    void isConfigured_false_whenBothBlank() {
        ScholarshipCsvClient client = newClient("", "");

        assertThat(client.isConfigured()).isFalse();
    }
}
