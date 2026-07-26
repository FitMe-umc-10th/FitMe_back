package umc.fitme.domain.user.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum SavedPostErrorCode implements BaseErrorCode {

    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "USER4005", "유효하지 않은 공고 카테고리입니다."),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "SAVED_POST4002", "유효하지 않은 정렬 조건입니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "SAVED_POST4003", "page 또는 size 값이 올바르지 않습니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "SAVED_POST4004", "잘못된 cursor 값입니다."),
    ALREADY_SAVED_POST(HttpStatus.BAD_REQUEST, "SAVED_POST4005", "이미 저장한 공고입니다."),
    ALREADY_UNSAVED_POST(HttpStatus.BAD_REQUEST, "SAVED_POST4006", "이미 저장 취소된 공고입니다."),
    SAVED_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "SAVED_POST4041", "저장한 공고를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
