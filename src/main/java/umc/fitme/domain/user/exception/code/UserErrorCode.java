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
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER409_1", "이미 가입된 이메일입니다."),

    PROFILE_UPDATE_EMPTY(HttpStatus.BAD_REQUEST, "USER400_1", "수정할 항목이 없습니다."),
    INVALID_GPA(HttpStatus.BAD_REQUEST, "USER400_2", "학점은 0.00 이상 4.50 이하, 소수점 2자리까지만 허용됩니다."),
    INVALID_INCOME_BRACKET(HttpStatus.BAD_REQUEST, "USER400_3", "소득구간은 1 이상 10 이하의 정수만 허용됩니다."),
    INTEREST_REQUIRED(HttpStatus.BAD_REQUEST, "USER400_4", "관심 분야는 최소 1개 이상 선택해야 합니다."),
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST404_1", "존재하지 않는 관심 분야가 포함되어 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
