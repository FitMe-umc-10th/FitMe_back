package umc.fitme.global.security.dto;

import lombok.RequiredArgsConstructor;
import umc.fitme.domain.user.enums.SocialType;

@RequiredArgsConstructor
public class KakaoResponse implements OAuth2Response{

    private final String providerId;
    private final String email;
    private final String name;

    @Override
    public SocialType getProvider() {
        return SocialType.KAKAO;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }
}
