package umc.fitme.domain.post.service;

import java.util.Set;

/**
 * 마감 임박 공고 매칭에 필요한 사용자 정보입니다.
 * 로그인/온보딩 기능이 병합되면 실제 사용자 정보에서 이 값들을 구성합니다.
 */
public record UserFitProfile(
        Double gpa,
        Integer incomeBracket,
        String region,
        String university,
        Set<String> interestFields
) {
    public UserFitProfile {
        interestFields = interestFields == null ? Set.of() : Set.copyOf(interestFields);
    }
}
