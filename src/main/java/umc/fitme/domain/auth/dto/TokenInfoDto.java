package umc.fitme.domain.auth.dto;

import lombok.Builder;

public class TokenInfoDto {

    @Builder
    public record TokenInfoRes(
            ATInfo info,
            String refreshToken
    ){}

    @Builder
    public record ATInfo(
            String accessToken
    ){}
}
