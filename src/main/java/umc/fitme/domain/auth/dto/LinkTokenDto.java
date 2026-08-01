package umc.fitme.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinkTokenDto {
    private Long userId;
    private String email;
    private String socialType;
    private String providerId;
}
