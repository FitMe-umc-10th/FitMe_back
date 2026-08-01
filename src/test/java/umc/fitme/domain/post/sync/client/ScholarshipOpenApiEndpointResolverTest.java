package umc.fitme.domain.post.sync.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScholarshipOpenApiEndpointResolverTest {

    @Test
    @DisplayName("summary의 날짜가 가장 최신인 path를 고른다")
    void pickLatestPath_choosesMostRecentDate() {
        Map<String, String> summaryByPath = new LinkedHashMap<>();
        summaryByPath.put("/15028252/v1/uddi:a", "한국장학재단_학자금지원정보(대학생)_20260511");
        summaryByPath.put("/15028252/v1/uddi:b", "한국장학재단_학자금지원정보(대학생)_20260722");
        summaryByPath.put("/15028252/v1/uddi:c", "한국장학재단_학자금지원정보(대학생)_20260612");

        String latest = ScholarshipOpenApiEndpointResolver.pickLatestPath(summaryByPath);

        assertThat(latest).isEqualTo("/15028252/v1/uddi:b");
    }

    @Test
    @DisplayName("날짜를 뽑을 수 없는 항목이 섞여 있어도 날짜가 있는 항목이 우선한다")
    void pickLatestPath_prefersDatedOverUndated() {
        Map<String, String> summaryByPath = new LinkedHashMap<>();
        summaryByPath.put("/15028252/v1/uddi:a", "한국장학재단_학자금지원정보_10/12/2021");
        summaryByPath.put("/15028252/v1/uddi:b", "한국장학재단_학자금지원정보_20210128");

        String latest = ScholarshipOpenApiEndpointResolver.pickLatestPath(summaryByPath);

        assertThat(latest).isEqualTo("/15028252/v1/uddi:b");
    }

    @Test
    @DisplayName("날짜를 아무 것도 뽑을 수 없으면 문서에 마지막으로 등록된 항목을 고른다")
    void pickLatestPath_fallsBackToDocumentOrder() {
        Map<String, String> summaryByPath = new LinkedHashMap<>();
        summaryByPath.put("/15028252/v1/uddi:a", "한국장학재단_학자금지원정보");
        summaryByPath.put("/15028252/v1/uddi:b", "한국장학재단_학자금지원정보(개정)");

        String latest = ScholarshipOpenApiEndpointResolver.pickLatestPath(summaryByPath);

        assertThat(latest).isEqualTo("/15028252/v1/uddi:b");
    }
}
