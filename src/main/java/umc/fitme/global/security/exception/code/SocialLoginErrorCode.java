package umc.fitme.global.security.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum SocialLoginErrorCode implements BaseErrorCode {

    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,
            "SOCIAL_LOGIN400_1",
            "기존 이메일이 이미 존재하여 계정 충돌이 일어났습니다."),
    EMAIL_CONFLICT(HttpStatus.BAD_REQUEST,
            "SOCIAL_LOGIN400_2",
            "이미 다른 로그인 수단으로 가입된 이메일입니다. 기존 계정과 연동하시겠습니까?"),
    UNVERIFIED_EMAIL(HttpStatus.BAD_REQUEST,
            "SOCIAL_LOGIN400_3",
            "검증되지 않은 카카오 이메일입니다."),
    DELETED_USER_EMAIL(HttpStatus.UNAUTHORIZED,
            "SOCIAL_LOGIN401_1",
            "탈퇴 처리된 계정입니다. 고객센터에 문의해주세요."),
    PROVIDER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "SOCIAL_LOGIN404_1",
            "해당되는 소셜로그인 방법이 존재하지 않습니다."),
    USER_INFO_NOT_FOUND(HttpStatus.NOT_FOUND,
            "SOCIAL_LOGIN404_2",
            "해당되는 유저 정보가 존재하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
