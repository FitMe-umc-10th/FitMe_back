package umc.fitme.global.apiPayload.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public class ProjectException extends RuntimeException {
    private final BaseErrorCode errorCode;
}
