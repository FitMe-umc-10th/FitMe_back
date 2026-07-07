package umc.fitme.global.security.dto;

import org.springframework.stereotype.Component;
import umc.fitme.domain.user.enums.SocialType;

@Component
public interface OAuth2Response {
    SocialType getProvider();
    String getProviderId();
    String getEmail();
    String getName();
}
