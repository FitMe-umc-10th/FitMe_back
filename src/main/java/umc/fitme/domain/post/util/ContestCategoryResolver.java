package umc.fitme.domain.post.util;

import org.springframework.stereotype.Component;
import umc.fitme.domain.post.enums.ContestCategory;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 사용자가 온보딩에서 고른 관심 분야 문자열을 공모전 카테고리로 바꿔 준다.
 * <p>
 * 공모전에는 {@link ContestCategory} enum이 붙어 있는데 사용자 관심 분야는
 * {@code interest.interest_name}에 자유 문자열로 저장된다(온보딩의 태그 선택과 직접 입력이 함께 들어온다).
 * 그대로는 비교할 수 없어서 이 클래스가 사이를 잇는다.
 * <p>
 * 매핑에 없는 문자열은 {@code null}을 돌려준다. 직접 입력한 관심 분야는 대부분 여기에 해당하며,
 * 호출부는 이를 '판정 불가'로 보고 공고를 걸러내지 않는다.
 */
@Component
public class ContestCategoryResolver {

    /**
     * 온보딩 화면이 제공하는 관심 분야 태그 6개. 화면 설계서 기준이며 이 값이 판정의 기준이다.
     */
    private static final Map<String, ContestCategory> ONBOARDING_TAGS = new LinkedHashMap<>();

    static {
        ONBOARDING_TAGS.put("마케팅", ContestCategory.MARKETING);
        ONBOARDING_TAGS.put("기획/아이디어", ContestCategory.PM);
        ONBOARDING_TAGS.put("디자인", ContestCategory.DESIGN);
        ONBOARDING_TAGS.put("it/개발", ContestCategory.IT);
        ONBOARDING_TAGS.put("영상편집", ContestCategory.VIDEO);
        ONBOARDING_TAGS.put("어학", ContestCategory.LANGUAGE);
    }

    /**
     * 직접 입력한 관심 분야를 위한 보조 키워드.
     * 온보딩 태그로 판정되지 않았을 때만 쓰며, 여기서도 못 찾으면 판정하지 않는다.
     */
    private static final Map<String, ContestCategory> KEYWORD_TO_CATEGORY = new LinkedHashMap<>();

    static {
        // 기타를 먼저 확인한다. 사용자가 기타를 고르면 모든 카테고리를 보여 주기 때문에 판정이 갈린다.
        KEYWORD_TO_CATEGORY.put("기타", ContestCategory.ETC);

        KEYWORD_TO_CATEGORY.put("기획", ContestCategory.PM);
        KEYWORD_TO_CATEGORY.put("pm", ContestCategory.PM);
        KEYWORD_TO_CATEGORY.put("프로덕트", ContestCategory.PM);
        KEYWORD_TO_CATEGORY.put("창업", ContestCategory.PM);
        KEYWORD_TO_CATEGORY.put("아이디어", ContestCategory.PM);

        KEYWORD_TO_CATEGORY.put("마케팅", ContestCategory.MARKETING);
        KEYWORD_TO_CATEGORY.put("광고", ContestCategory.MARKETING);
        KEYWORD_TO_CATEGORY.put("홍보", ContestCategory.MARKETING);
        KEYWORD_TO_CATEGORY.put("브랜딩", ContestCategory.MARKETING);

        KEYWORD_TO_CATEGORY.put("디자인", ContestCategory.DESIGN);
        KEYWORD_TO_CATEGORY.put("ux", ContestCategory.DESIGN);
        KEYWORD_TO_CATEGORY.put("ui", ContestCategory.DESIGN);
        KEYWORD_TO_CATEGORY.put("그래픽", ContestCategory.DESIGN);
        KEYWORD_TO_CATEGORY.put("미술", ContestCategory.DESIGN);

        KEYWORD_TO_CATEGORY.put("소프트웨어", ContestCategory.IT);
        KEYWORD_TO_CATEGORY.put("개발", ContestCategory.IT);
        KEYWORD_TO_CATEGORY.put("프로그래밍", ContestCategory.IT);
        KEYWORD_TO_CATEGORY.put("인공지능", ContestCategory.IT);
        KEYWORD_TO_CATEGORY.put("데이터", ContestCategory.IT);
        KEYWORD_TO_CATEGORY.put("보안", ContestCategory.IT);
        KEYWORD_TO_CATEGORY.put("it", ContestCategory.IT);
        KEYWORD_TO_CATEGORY.put("ai", ContestCategory.IT);

        KEYWORD_TO_CATEGORY.put("영상", ContestCategory.VIDEO);
        KEYWORD_TO_CATEGORY.put("미디어", ContestCategory.VIDEO);
        KEYWORD_TO_CATEGORY.put("숏폼", ContestCategory.VIDEO);
        KEYWORD_TO_CATEGORY.put("유튜브", ContestCategory.VIDEO);
        KEYWORD_TO_CATEGORY.put("사진", ContestCategory.VIDEO);
        KEYWORD_TO_CATEGORY.put("영화", ContestCategory.VIDEO);
    }

    /**
     * 관심 분야 문자열 하나를 카테고리로 바꾼다. 알아볼 수 없으면 {@code null}.
     * <p>
     * 온보딩 태그와 정확히 일치하는지를 먼저 보고, 아니면 직접 입력으로 보아 키워드로 찾는다.
     * 태그를 먼저 확인하므로 보조 키워드를 나중에 손봐도 태그 판정은 흔들리지 않는다.
     */
    public ContestCategory resolve(String interestName) {
        String normalized = normalize(interestName);
        if (normalized.isEmpty()) {
            return null;
        }

        ContestCategory tagMatch = ONBOARDING_TAGS.get(normalized);
        if (tagMatch != null) {
            return tagMatch;
        }

        // 'it', 'ai'처럼 짧은 영문 키워드가 다른 단어에 묻히지 않도록 토큰 단위를 먼저 본다.
        for (String token : normalized.split("/")) {
            ContestCategory exact = KEYWORD_TO_CATEGORY.get(token);
            if (exact != null) {
                return exact;
            }
        }

        for (Map.Entry<String, ContestCategory> entry : KEYWORD_TO_CATEGORY.entrySet()) {
            String keyword = entry.getKey();
            // 두 글자 이하 영문 키워드는 부분 일치로 찾으면 오탐이 생기므로 토큰 일치로만 인정한다.
            if (keyword.length() <= 2) {
                continue;
            }
            if (normalized.contains(keyword)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 사용자의 관심 분야 여러 개를 카테고리 집합으로 바꾼다.
     * 알아보지 못한 항목은 결과에서 빠지므로, 전부 알아보지 못하면 빈 집합이 된다.
     */
    public Set<ContestCategory> resolveAll(Set<String> interestNames) {
        if (interestNames == null || interestNames.isEmpty()) {
            return Set.of();
        }

        Set<ContestCategory> categories = new java.util.LinkedHashSet<>();
        for (String name : interestNames) {
            ContestCategory category = resolve(name);
            if (category != null) {
                categories.add(category);
            }
        }
        return categories;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
