package umc.fitme.global.security.dto;

import lombok.RequiredArgsConstructor;
import umc.fitme.domain.user.enums.SocialType;

import java.util.Map;

@RequiredArgsConstructor
public class NaverResponse implements OAuth2Response{

    private final String providerId;
    private final Map<String, Object> attributes;

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
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getBirthday() {
        return (String) attributes.get("birthday");
    }

    @Override
    public String getBirthyear() {
        return (String) attributes.get("birthyear");
    }
}
