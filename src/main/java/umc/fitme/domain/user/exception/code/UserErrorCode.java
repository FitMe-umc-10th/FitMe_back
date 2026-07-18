package umc.fitme.domain.user.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404_1", "사용자를 찾을 수 없습니다."),
    USER_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_DETAIL404_1", "해당 회원의 프로필을 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER409_1", "이미 가입된 이메일입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
