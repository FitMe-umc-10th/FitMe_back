package umc.fitme.domain.onboarding.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum OnboardingSuccessCode implements BaseSuccessCode {

    ONBOARDING_COMPLETE(HttpStatus.OK, "ONBOARDING200_1", "온보딩 정보 저장에 성공했습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
