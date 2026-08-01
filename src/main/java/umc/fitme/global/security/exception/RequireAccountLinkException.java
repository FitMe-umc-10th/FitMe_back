package umc.fitme.global.security.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import umc.fitme.domain.user.enums.SocialType;

@Getter
public class RequireAccountLinkException extends AuthenticationException {

    private final Long userId;
    private final String email;
    private final SocialType provider;
    private final String providerId;

    public RequireAccountLinkException(String msg, Long userId, String email, SocialType provider, String providerId) {
        super(msg);
        this.userId = userId;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
    }
}
