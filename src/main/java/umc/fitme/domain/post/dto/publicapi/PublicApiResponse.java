package umc.fitme.domain.post.dto.publicapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class PublicApiResponse {

    // JSON 키값과 변수명이 같으면 @JsonProperty를 생략해도 문제 안됨
    private Integer page;
    private Integer perPage;
    private Integer totalCount;
    private Integer currentCount;
    private Integer matchCount;

    // 위에서 만든 PublicApiScholarshipDTO 타입의 리스트로 선언
    @JsonProperty("data")
    private List<PublicApiScholarshipDTO> data;
}