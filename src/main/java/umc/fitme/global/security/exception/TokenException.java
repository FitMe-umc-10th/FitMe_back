package umc.fitme.global.security.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import umc.fitme.global.security.exception.code.TokenErrorCode;

@Getter
public class TokenException extends AuthenticationException {

  private final TokenErrorCode errorCode;

  public TokenException(TokenErrorCode errorCode) {
    super(errorCode.getMessage()); // 시큐리티에게 에러 메시지 전달
    this.errorCode = errorCode;
  }
}
