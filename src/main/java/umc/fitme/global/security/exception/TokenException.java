package umc.fitme.global.security.exception;

import lombok.Getter;
import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

@Getter
public class TokenException extends ProjectException {

  public TokenException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
