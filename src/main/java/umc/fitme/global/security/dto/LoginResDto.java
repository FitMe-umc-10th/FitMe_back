package umc.fitme.global.security.dto;

import lombok.Builder;

@Builder
public record LoginResDto(
        String accessToken,
        String refreshToken,
        UserInfo userInfo
){
    @Builder
    public record UserInfo(
            Long userId,
            String name,
            Boolean isOnboarded
    ){}
}
