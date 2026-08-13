package umc.fitme.global.apiPayload.exception;

import lombok.Getter;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
public class ProjectException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public ProjectException(BaseErrorCode errorCode) {
        // super(message)를 호출하지 않으면 getMessage()가 항상 null이 되어
        // 로그에 예외 사유가 남지 않는다.
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
