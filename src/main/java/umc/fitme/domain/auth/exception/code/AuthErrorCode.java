package umc.fitme.domain.auth.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    CODE_NOT_VALIDATE(HttpStatus.BAD_REQUEST,
            "AUTH400_1",
            "해당 인증번호는 만료되었습니다. 발급된 시간 기준 5분 이내로 인증번호를 입력해주세요."),
    CODE_NOT_MATCH(HttpStatus.BAD_REQUEST,
            "AUTH400_2",
            "올바르지 않는 인증번호 입니다. 정확한 인증번호 6자리를 입력해주세요."),
    CODE_ALREADY_USED(HttpStatus.BAD_REQUEST,
            "AUTH400_3",
            "이미 이메일이 인증되었습니다."),
    NEED_TO_VERIFY(HttpStatus.BAD_REQUEST,
            "AUTH400_4",
            "이메일 인증을 먼저 완료해주세요."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST,
            "AUTH400_5",
            "비밀번호가 일치하지 않습니다."),
    NEED_TO_AGREE(HttpStatus.BAD_REQUEST,
            "AUTH400_6",
            "개인정보 보호약관 동의는 필수입니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND,
            "AUTH404_1",
            "해당 메일로 보낸 인증번호가 존재하지 않습니다. 먼저 원하는 이메일로 인증번호를 보내주세요."),
    EMAIL_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE,
                "AUTH503_1",
            "이메일 발송에 실패하였습니다. 메일 서버 장애"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
