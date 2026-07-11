package umc.fitme.global.security.dto;

import umc.fitme.domain.user.enums.SocialType;

public interface OAuth2Response {
    SocialType getProvider();
    String getProviderId();
    String getEmail();
    String getName();
}
