package umc.fitme.domain.post.sync.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ScholarshipApiClientTest {

    private ScholarshipApiClient newClient(String serviceKey) {
        ScholarshipApiClient client = new ScholarshipApiClient(
                mock(RestClient.class), mock(ScholarshipOpenApiEndpointResolver.class));
        ReflectionTestUtils.setField(client, "serviceKey", serviceKey);
        return client;
    }

    @Test
    @DisplayName("service-key가 있으면 설정된 것으로 본다")
    void isConfigured_true_whenServiceKeyPresent() {
        ScholarshipApiClient client = newClient("service-key");

        assertThat(client.isConfigured()).isTrue();
    }

    @Test
    @DisplayName("service-key가 비어 있으면 설정되지 않은 것으로 본다")
    void isConfigured_false_whenServiceKeyBlank() {
        ScholarshipApiClient client = newClient("");

        assertThat(client.isConfigured()).isFalse();
    }
}
