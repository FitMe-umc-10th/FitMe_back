package umc.fitme.domain.auth.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    EMAIL_SEND_FAILED(HttpStatus.EXPECTATION_FAILED,
    "AUTH417_1",
            "이메일 발송에 실패하였습니다.")
    ;
    private final HttpStatus status;
    private final String code;
    private final String message;
}
