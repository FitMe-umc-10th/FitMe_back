package umc.fitme.domain.onboarding.exception;

import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

public class OnboardingException extends ProjectException {
    public OnboardingException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
