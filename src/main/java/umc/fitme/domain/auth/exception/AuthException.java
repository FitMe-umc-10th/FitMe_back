package umc.fitme.domain.auth.exception;

import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

public class AuthException extends ProjectException {
    public AuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
