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
    USER_RECENT_SEARCH_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_RECENT_SEARCH404_1", "해당 회원의 최근 검색어를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER400_2", "이미 가입된 이메일입니다. 가입한 로그인 방식으로 로그인 해주세요."),
    PROFILE_UPDATE_CONFLICT(HttpStatus.CONFLICT, "USER409_2", "다른 요청이 프로필을 먼저 수정했습니다. 다시 시도해 주세요."),

    PROFILE_UPDATE_EMPTY(HttpStatus.BAD_REQUEST, "USER400_1", "수정할 항목이 없습니다."),
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST404_1", "존재하지 않는 관심 분야가 포함되어 있습니다."),

    NOTIFICATION_UPDATE_EMPTY(HttpStatus.BAD_REQUEST, "NOTIFICATION400_1", "수정할 알림 설정 값이 없습니다."),

    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "IMAGE400_1", "지원하지 않는 이미지 형식입니다. (jpg, jpeg, png만 허용됩니다.)"),
    INVALID_IMAGE_FILE_NAME(HttpStatus.BAD_REQUEST, "IMAGE400_2", "파일 확장자를 확인할 수 없습니다."),
    INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "IMAGE400_3", "파일 용량이 너무 큽니다. (최대 5MB)"),
    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "IMAGE400_4", "유효하지 않은 이미지 URL입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
