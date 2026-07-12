package umc.fitme.domain.post.dto.publicapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PublicApiScholarshipDTO {

    // 외부 API의 JSON 키값과 내부 변수명을 매핑
    @JsonProperty("상품명")
    private String title;

    @JsonProperty("운영기관명")
    private String organizer;

    @JsonProperty("모집시작일")
    private String startDate;

    @JsonProperty("모집종료일")
    private String deadlineDate;

    @JsonProperty("지원내역 상세내용")
    private String summary;

    @JsonProperty("홈페이지 주소")
    private String applicationUrl;
}