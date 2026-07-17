package umc.fitme.domain.user.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserApplicationErrorCode implements BaseErrorCode {

    USER_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_APPLICATION404_1", "지원 이력을 찾을 수 없습니다."),
    INVALID_USER_APPLICATION_TAB(HttpStatus.BAD_REQUEST, "USER_APPLICATION400_1", "유효하지 않은 이력 탭입니다."),
    INVALID_USER_APPLICATION_STATUS(HttpStatus.BAD_REQUEST, "USER_APPLICATION400_2", "변경할 수 없는 상태입니다."),
    MEMO_TOO_LONG(HttpStatus.BAD_REQUEST, "USER_APPLICATION400_3", "메모는 최대 1,000자를 초과할 수 없습니다."),
    USER_APPLICATION_SNAPSHOT_NOT_FOUND(
            HttpStatus.NOT_FOUND, "USER_APPLICATION404_2", "지원 이력 스냅샷을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
