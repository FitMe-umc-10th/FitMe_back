package umc.fitme.domain.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class SavedPostRequestDto {

    @Builder
    public record SavePostRequest(
            @NotNull(message = "postId는 필수입니다.")
            Long postId
    ) {

    }
}
