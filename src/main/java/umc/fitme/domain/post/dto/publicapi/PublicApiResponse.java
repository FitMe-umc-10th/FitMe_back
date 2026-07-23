package umc.fitme.domain.post.dto.publicapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class PublicApiResponse {

    private Integer page;
    private Integer perPage;
    private Integer totalCount;
    private Integer currentCount;
    private Integer matchCount;

    @JsonProperty("data")
    private List<PublicApiScholarshipDTO> data;
}