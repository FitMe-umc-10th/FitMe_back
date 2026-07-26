package umc.fitme.global.security.dto;

import lombok.RequiredArgsConstructor;
import umc.fitme.domain.user.enums.SocialType;

@RequiredArgsConstructor
public class NaverResponse implements OAuth2Response{

    private final String providerId;
    private final String email;
    private final String name;

    @Override
    public SocialType getProvider() {
        return SocialType.NAVER;
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
