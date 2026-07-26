package umc.fitme.domain.user.exception;

import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

public class UserException extends ProjectException {
    public UserException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
