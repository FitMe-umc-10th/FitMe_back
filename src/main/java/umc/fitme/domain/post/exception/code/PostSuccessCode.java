package umc.fitme.domain.post.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum PostSuccessCode implements BaseSuccessCode {
    SEARCH_MAIN_OK(HttpStatus.OK,
            "POST200_1",
            "검색 대시보드 조회가 성공적으로 반환되었습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
