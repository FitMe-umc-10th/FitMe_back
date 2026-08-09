package umc.fitme.domain.post.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 사용자가 입력한 대학 이름을 대학 구분(4년제/전문대/원격대학/기술대학)으로 바꿔 준다.
 * <p>
 * 공고의 대학 조건에는 대학 구분이 들어 있는데(예: {@code 4년제(5~6년제포함)전문대(2~3년제)})
 * 사용자에게는 대학 이름만 있어서(예: {@code 가천대학교}) 그대로는 비교할 수 없다.
 * 이 클래스가 그 사이를 잇는다.
 * <p>
 * 원본은 공공데이터포털 '전국대학및전문대학정보표준데이터'이며
 * {@code resources/data/university.csv}에 학교명·구분·시도·시군구 네 컬럼으로 담겨 있다.
 * 데이터가 418건뿐이고 갱신이 연 단위라 DB 대신 리소스로 두고 기동 시 한 번만 읽는다.
 */
@Component
public class UniversityTypeResolver {

    private static final String CSV_PATH = "data/university.csv";

    /**
     * 공고 조건 문자열에 나타나는 대학 구분 어휘.
     * 앞의 네 개는 CSV '구분' 컬럼의 값과 같고, 뒤의 세 개는 공고 조건에만 등장한다.
     * 조건에 이 중 하나라도 있으면 구분으로 판정할 수 있는 조건으로 본다.
     */
    private static final List<String> TYPE_KEYWORDS = List.of(
            "4년제", "전문대", "원격대학", "기술대학",
            "해외대학", "대학원", "학점은행제"
    );

    /** 정규화한 학교명 -> 구분. */
    private final Map<String, String> typeByName;

    public UniversityTypeResolver() {
        this.typeByName = loadCsv();
    }

    /**
     * 대학 이름을 구분으로 바꾼다. 목록에 없으면 {@code null}을 돌려준다.
     * 온보딩이 자유 입력이라 약칭·공백·캠퍼스 표기가 섞여 들어오므로 몇 가지 형태를 함께 시도한다.
     */
    public String resolveType(String universityName) {
        String normalized = normalize(universityName);
        if (normalized.isEmpty()) {
            return null;
        }

        for (String candidate : nameCandidates(normalized)) {
            String type = typeByName.get(candidate);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    /**
     * 공고의 대학 조건에 구분 키워드가 하나라도 들어 있는지 확인한다.
     * {@code 도내 대학 해외교환 장학생}처럼 문장으로 적힌 조건은 기계적으로 판정할 수 없으므로
     * 호출부에서 이 값이 false면 제한 없는 조건으로 취급한다.
     */
    public boolean containsTypeKeyword(String requirement) {
        String normalized = normalize(requirement);
        if (normalized.isEmpty()) {
            return false;
        }
        return TYPE_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    /** 적재된 학교 수. 로딩이 됐는지 확인하는 용도로 쓴다. */
    public int size() {
        return typeByName.size();
    }

    /**
     * '가천대', '가천대학'처럼 줄여 쓴 이름도 찾을 수 있도록 후보를 넓힌다.
     * 원래 형태를 가장 먼저 시도하므로 정식 명칭이 들어오면 한 번에 맞는다.
     */
    private List<String> nameCandidates(String normalized) {
        List<String> candidates = new ArrayList<>();
        candidates.add(normalized);

        if (normalized.endsWith("대학교")) {
            return candidates;
        }
        if (normalized.endsWith("대학")) {
            // 가천대학 -> 가천대학교
            candidates.add(normalized + "교");
        } else if (normalized.endsWith("대")) {
            // 가천대 -> 가천대학교 / 가천대학
            candidates.add(normalized + "학교");
            candidates.add(normalized + "학");
        } else {
            // 가천 -> 가천대학교 / 가천대학
            candidates.add(normalized + "대학교");
            candidates.add(normalized + "대학");
        }
        return candidates;
    }

    /**
     * 학교명 비교용 정규화. 공백과 괄호 안 캠퍼스 표기를 지운다.
     * {@code 가야대학교(고령)}과 {@code 가야대학교(김해)}가 같은 키가 되지만
     * 원본 418건에서 괄호를 지웠을 때 구분이 서로 달라지는 학교는 없어 충돌하지 않는다.
     */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\([^)]*\\)", "")
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);
    }

    private Map<String, String> loadCsv() {
        Map<String, String> result = new HashMap<>();
        ClassPathResource resource = new ClassPathResource(CSV_PATH);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            reader.readLine(); // 헤더: 학교명,구분,시도,시군구
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] columns = line.split(",", -1);
                if (columns.length < 2) {
                    continue;
                }

                String name = normalize(columns[0]);
                String type = columns[1].trim();
                if (name.isEmpty() || type.isEmpty()) {
                    continue;
                }
                result.putIfAbsent(name, type);
            }
        } catch (IOException e) {
            // 대학 목록을 못 읽으면 구분을 판정할 수 없다.
            // 이때는 모든 대학 조건이 '판정 불가'가 되어 공고가 걸러지지 않고 그대로 노출된다.
            throw new IllegalStateException("대학 목록을 읽지 못했습니다: " + CSV_PATH, e);
        }
        return result;
    }
}
