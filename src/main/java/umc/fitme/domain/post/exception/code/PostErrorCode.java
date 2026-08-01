package umc.fitme.domain.post.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements BaseErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST404_1", "공고를 찾을 수 없습니다."),
    POST_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "POST404_2", "해당 공고 타입이 존재하지 않습니다. (ALL, SCHOLARSHIP, CONTEST 중 입력해주세요)"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
