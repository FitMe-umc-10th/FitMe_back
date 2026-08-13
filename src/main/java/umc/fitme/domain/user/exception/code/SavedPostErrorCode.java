package umc.fitme.domain.user.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum SavedPostErrorCode implements BaseErrorCode {

    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "SAVED_POST400_1", "유효하지 않은 공고 카테고리입니다."),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "SAVED_POST400_2", "유효하지 않은 정렬 조건입니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "SAVED_POST400_3", "page 또는 size 값이 올바르지 않습니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "SAVED_POST400_4", "잘못된 cursor 값입니다."),
    SAVED_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "SAVED_POST404_1", "저장한 공고를 찾을 수 없습니다."),
    ALREADY_SAVED_POST(HttpStatus.CONFLICT, "SAVED_POST409_1", "이미 저장한 공고입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
