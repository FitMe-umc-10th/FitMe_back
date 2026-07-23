package umc.fitme.domain.post.dto.publicapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PublicApiScholarshipDTO {

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

    @JsonProperty("성적기준")
    private String gradeRequirement;

    @JsonProperty("소득기준")
    private String incomeRequirement;

    @JsonProperty("지역기준")
    private String regionRequirement;
}