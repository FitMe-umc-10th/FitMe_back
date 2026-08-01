package umc.fitme.global.security.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum TokenErrorCode implements BaseErrorCode {

    LINK_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "TOKEN400_1","linkToken이 만료되었습니다." ),
    AT_NOT_VALIDATE(HttpStatus.BAD_REQUEST, "TOKEN400_2", "토큰 타입을 확인해주세요. 타입은 access만 가능합니다."),
    RT_NOT_VALIDATE(HttpStatus.BAD_REQUEST, "TOKEN400_3", "토큰 타입을 확인해주세요. 타입은 refresh만 가능합니다."),
    LT_NOT_VALIDATE(HttpStatus.BAD_REQUEST, "TOKEN400_4", "토큰 타입을 확인해주세요. 타입은 link만 가능합니다."),
    INVALID_LINK_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN400_5", "linkToken이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST,"TOKEN400_6" ,"해당 RT가 유효하지 않습니다." ),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,"TOKEN403_1","AT가 만료되었습니다. 재발급을 요청하세요."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN403_2","RT가 만료되었습니다. 재로그인을 해주세요."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED,"TOKEN403_3" ,"유효하지 않은 토큰입니다. 다시 로그인해주세요."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND,"TOKEN404_1" , "쿠키가 비어있습니다. 쿠키에 RT 값이 들어있는지 확인하세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
