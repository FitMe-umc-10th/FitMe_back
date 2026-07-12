package umc.fitme.domain.user.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum UserApplicationSuccessCode implements BaseSuccessCode {

    DELETE_USER_APPLICATION_SUCCESS(
            HttpStatus.OK,
            "USER_APPLICATION200_4",
            "지원 이력을 삭제했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
