package umc.fitme.domain.notify.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum NotificationSuccessCode implements BaseSuccessCode {

    GET_SETTING(HttpStatus.OK, "NOTIFICATION200_1", "알림 설정이 성공적으로 조회되었습니다."),
    UPDATE_SETTING(HttpStatus.OK, "NOTIFICATION200_2", "알림 설정이 수정되었습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}