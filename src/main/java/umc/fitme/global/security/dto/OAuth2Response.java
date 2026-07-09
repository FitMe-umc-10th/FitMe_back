package umc.fitme.global.security.dto;

import org.springframework.stereotype.Component;
import umc.fitme.domain.user.enums.SocialType;

package umc.fitme.global.security.dto;

import umc.fitme.domain.user.enums.SocialType;

public interface OAuth2Response {
    SocialType getProvider();
    String getProviderId();
    String getEmail();
    String getName();
}
    SocialType getProvider();
    String getProviderId();
    String getEmail();
    String getName();
}
