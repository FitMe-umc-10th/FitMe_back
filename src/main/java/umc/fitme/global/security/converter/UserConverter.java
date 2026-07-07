package umc.fitme.global.security.converter;

import org.springframework.stereotype.Component;
import umc.fitme.domain.user.entity.User;
import umc.fitme.global.security.dto.OAuth2Response;

@Component
public class UserConverter {

    public User convert(OAuth2Response oAuth2Response){
        return User.builder()
                .email(oAuth2Response.getEmail())
                .name(oAuth2Response.getName())
                .socialType(oAuth2Response.getProvider())
                .socialUid(oAuth2Response.getProviderId())
                .build();
    }
}
