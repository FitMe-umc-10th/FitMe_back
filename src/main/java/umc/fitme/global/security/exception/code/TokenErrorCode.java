package umc.fitme.global.security.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum TokenErrorCode implements BaseErrorCode {

    AT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "TOKEN400_1", "토큰 타입을 확인해주세요. 타입은 access만 가능합니다."),
    RT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "TOKEN400_2", "토큰 타입을 확인해주세요. 타입은 refresh만 가능합니다."),
    LT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "TOKEN400_3", "토큰 타입을 확인해주세요. 타입은 link만 가능합니다."),
    AT_INVALID(HttpStatus.BAD_REQUEST,"TOKEN400_4" ,"유효하지 않은 AT 토큰입니다. 다시 로그인해주세요."),
    RT_INVALID(HttpStatus.BAD_REQUEST,"TOKEN400_5" ,"유효하지 않은 RT 토큰입니다. 쿠키에 담긴 RT를 확인해주세요." ),
    LT_INVALID(HttpStatus.BAD_REQUEST, "TOKEN400_6", "유효하지 않은 LT 토큰입니다. 계정 연동을 다시 진행해주세요."),
    AT_EXPIRED(HttpStatus.UNAUTHORIZED,"TOKEN401_1","AT가 만료되었습니다. 재발급을 요청하세요."),
    RT_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN401_2","RT가 만료되었습니다. 재로그인을 해주세요."),
    LT_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN401_3","LT가 만료되었습니다. 계정 연동 요청을 다시 시도해주세요." ),
    RT_NOT_FOUND(HttpStatus.NOT_FOUND,"TOKEN404_1" , "쿠키가 비어있습니다. 쿠키에 RT 값이 들어있는지 확인하세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
