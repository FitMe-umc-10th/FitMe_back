package umc.fitme.domain.auth.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {


    REQUEST_OK(HttpStatus.OK, "AUTH200_1", "인증번호 요청이 성공적으로 발송되었습니다."),
    CONFIRM_OK(HttpStatus.OK,"AUTH200_2" ,"인증번호 검증에 성공하였습니다."),
    SIGNUP_OK(HttpStatus.OK, "AUTH200_3", "회원가입이 성공적으로 완료되었습니다."),
    LOGIN_OK(HttpStatus.OK, "AUTH200_4", "로그인이 성공적으로 완료되었습니다."),
    LOGOUT_OK(HttpStatus.OK,"AUTH200_5" ,"로그아웃이 성공적으로 완료되었습니다."),
    LINK_ACCOUNT_OK(HttpStatus.OK,"AUTH200_6", "계정 연동이 성공적으로 완료되었습니다."),
    REISSUE_OK(HttpStatus.OK, "AUTH200_7", "새로운 AT, RT가 성공적으로 재발급되었습니다."),
    DELETE_OK(HttpStatus.OK,"AUTH200_8" ,"회원 탈퇴가 성공적으로 완료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
