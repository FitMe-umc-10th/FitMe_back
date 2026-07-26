package umc.fitme.domain.notify.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum AnnouncementErrorCode implements BaseErrorCode {

    ANNOUNCEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ANNOUNCEMENT404", "존재하지 않는 공지사항입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}