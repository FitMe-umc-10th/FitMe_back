package umc.fitme.domain.user.enums;

import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.code.InvalidValueErrorCodeProvider;
import umc.fitme.domain.user.exception.code.SavedPostErrorCode;

public enum SavedPostCategory implements InvalidValueErrorCodeProvider {
    ALL,
    SCHOLARSHIP,
    CONTEST;

    @Override
    public BaseErrorCode invalidValueErrorCode() {
        return SavedPostErrorCode.INVALID_CATEGORY;
    }
}
