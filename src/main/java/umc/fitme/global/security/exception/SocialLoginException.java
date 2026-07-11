package umc.fitme.global.security.exception;

import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

public class SocialLoginException extends ProjectException {
    public SocialLoginException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
