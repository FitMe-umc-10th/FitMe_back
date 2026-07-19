package umc.fitme.domain.notify.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {

    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "NOTIFICATION400_1", "올바르지 않은 이메일 형식입니다."),
    INVALID_TOGGLE_VALUE(HttpStatus.BAD_REQUEST, "NOTIFICATION400_2", "잘못된 요청 형식입니다."),
    UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION500_1", "알림 설정 수정에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}