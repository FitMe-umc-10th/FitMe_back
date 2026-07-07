package umc.fitme.global.security.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum SocialLoginSuccessCode implements BaseSuccessCode {

    SOCIAL_LOGIN_SUCCESS(HttpStatus.OK, "LOGIN200_1", "로그인이 완료되었습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
