package umc.fitme.domain.post.exception;

import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

public class PostException extends ProjectException {
    public PostException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
