package umc.fitme.domain.notify.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {

    NOTIFICATION_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "NOTIFICATION401", "로그인이 필요한 기능입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
