package umc.fitme.domain.user.enums;

import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.code.InvalidValueErrorCodeProvider;
import umc.fitme.domain.user.exception.code.SavedPostErrorCode;

public enum SavedPostSort implements InvalidValueErrorCodeProvider {
    RECENT,
    DEADLINE;

    @Override
    public BaseErrorCode invalidValueErrorCode() {
        return SavedPostErrorCode.INVALID_SORT;
    }
}
