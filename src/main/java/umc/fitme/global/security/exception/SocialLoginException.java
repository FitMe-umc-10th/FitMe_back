package umc.fitme.global.security.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import umc.fitme.global.security.exception.code.SocialLoginErrorCode;

@Getter
public class SocialLoginException extends AuthenticationException {

    private final SocialLoginErrorCode errorCode;

    public SocialLoginException(SocialLoginErrorCode errorCode) {
        super(errorCode.getMessage()); // 시큐리티에게 에러 메시지 전달
        this.errorCode = errorCode;
    }
}
