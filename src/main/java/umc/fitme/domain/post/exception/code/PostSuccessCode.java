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
            "검색 대시보드 조회가 성공적으로 반환되었습니다."),
    SEARCH_POST_OK(HttpStatus.OK,
            "POST200_2",
            "조건을 기반으로 공고 검색이 성공적으로 조회되었습니다." ),
    DELETE_RECENT_KEYWORD_OK(HttpStatus.OK,
            "POST200_3",
            "유저의 최근 검색어가 성공적으로 삭제되었습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
