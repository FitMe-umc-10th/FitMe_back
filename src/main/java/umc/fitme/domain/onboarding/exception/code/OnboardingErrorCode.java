package umc.fitme.domain.onboarding.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum OnboardingErrorCode implements BaseErrorCode {

    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "ONBOARDING400_1", "필수 입력값이 누락되었습니다."),
    INVALID_GPA_RANGE(HttpStatus.BAD_REQUEST, "ONBOARDING400_2", "학점은 0.0 이상 4.5 이하 입니다."),
    INTEREST_REQUIRED(HttpStatus.BAD_REQUEST, "ONBOARDING400_3", "관심 분야는 최소 1개 이상 선택 또는 입력해야 합니다."),
    INVALID_INCOME_LEVEL(HttpStatus.BAD_REQUEST, "ONBOARDING400_4", "소득구간 형식이 올바르지 않습니다."),
    ALREADY_ONBOARDED(HttpStatus.CONFLICT, "ONBOARDING409_1", "이미 온보딩을 완료했습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
