package umc.fitme.global.security.dto;

import lombok.RequiredArgsConstructor;
import umc.fitme.domain.user.enums.SocialType;

import java.util.Map;

@RequiredArgsConstructor
public class KakaoResponse implements OAuth2Response{

    private final String providerId;
    private final Map<String, Object> attributes;

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
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> profile = (Map<String, Object>) attributes.get("profile");
        return (String) profile.get("nickname");
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
