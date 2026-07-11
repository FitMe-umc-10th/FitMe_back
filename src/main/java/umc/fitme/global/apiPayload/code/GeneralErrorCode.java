package umc.fitme.global.apiPayload.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400_1", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401_1", "인증되지 않았습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403_1", "접근이 금지되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404_1", "해당 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500_1", "서버 측 문제입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404_1", "사용자를 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST404_1", "공고를 찾을 수 없습니다."),
    USER_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_APPLICATION404_1", "지원 이력을 찾을 수 없습니다."),
    INVALID_USER_APPLICATION_TAB(HttpStatus.BAD_REQUEST, "USER_APPLICATION400_1", "유효하지 않은 이력 탭입니다."),
    INVALID_USER_APPLICATION_STATUS(HttpStatus.BAD_REQUEST, "USER_APPLICATION400_2", "변경할 수 없는 상태입니다."),
    MEMO_TOO_LONG(HttpStatus.BAD_REQUEST, "USER_APPLICATION400_3", "메모는 최대 1,000자를 초과할 수 없습니다."),

    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "SAVED_POST4003", "page 또는 size 값이 올바르지 않습니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "SAVED_POST4004", "잘못된 cursor 값입니다."),
    ALREADY_SAVED_POST(HttpStatus.BAD_REQUEST, "SAVED_POST4005", "이미 저장한 공고입니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "USER4005", "유효하지 않은 공고 카테고리입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
