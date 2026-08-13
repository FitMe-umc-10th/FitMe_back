package umc.fitme.global.apiPayload.code;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/***
 * 에러 코드 체계가 흐트러지는 것을 막는 회귀 방지 테스트.
 *
 * 코드 문자열이 도메인마다 제각각이거나(USER4005, ANNOUNCEMENT404 등) 서로 중복되면
 * 프론트엔드가 code로 분기할 수 없게 된다.
 */
class ErrorCodeConventionTest {

    // <도메인><HTTP상태>_<일련번호> (예: USER404_1, SAVED_POST409_1, COMMON400_1)
    private static final Pattern CODE_FORMAT = Pattern.compile("^[A-Z][A-Z_]*[A-Z](\\d{3})_(\\d+)$");

    private static final String BASE_PACKAGE = "umc.fitme";

    private List<BaseErrorCode> allErrorCodes() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(BaseErrorCode.class));

        List<BaseErrorCode> codes = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents(BASE_PACKAGE)) {
            try {
                Class<?> clazz = Class.forName(definition.getBeanClassName());
                if (!clazz.isEnum()) {
                    continue;
                }
                for (Object constant : clazz.getEnumConstants()) {
                    codes.add((BaseErrorCode) constant);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return codes;
    }

    @Test
    @DisplayName("에러 코드 enum을 하나 이상 수집한다 (스캔이 비어 통과하는 것을 방지)")
    void 에러코드를_수집한다() {
        assertThat(allErrorCodes()).hasSizeGreaterThan(40);
    }

    @Test
    @DisplayName("모든 에러 코드는 <도메인><상태>_<번호> 형식을 따른다")
    void 코드_형식을_따른다() {
        List<String> violations = allErrorCodes().stream()
                .map(BaseErrorCode::getCode)
                .filter(code -> !CODE_FORMAT.matcher(code).matches())
                .toList();

        assertThat(violations)
                .as("형식에 맞지 않는 에러 코드")
                .isEmpty();
    }

    @Test
    @DisplayName("코드 문자열에 박힌 상태값과 실제 HTTP 상태가 일치한다")
    void 코드의_상태값과_HTTP_상태가_일치한다() {
        List<String> violations = new ArrayList<>();

        for (BaseErrorCode errorCode : allErrorCodes()) {
            var matcher = CODE_FORMAT.matcher(errorCode.getCode());
            if (!matcher.matches()) {
                continue; // 형식 검증은 위 테스트가 담당한다
            }
            int statusInCode = Integer.parseInt(matcher.group(1));
            if (statusInCode != errorCode.getStatus().value()) {
                violations.add("%s (상태=%d)".formatted(errorCode.getCode(), errorCode.getStatus().value()));
            }
        }

        assertThat(violations)
                .as("코드 문자열과 HTTP 상태가 어긋난 에러 코드")
                .isEmpty();
    }

    @Test
    @DisplayName("에러 코드 문자열은 프로젝트 전체에서 중복되지 않는다")
    void 코드는_전체에서_중복되지_않는다() {
        Map<String, List<String>> byCode = new LinkedHashMap<>();
        for (BaseErrorCode errorCode : allErrorCodes()) {
            byCode.computeIfAbsent(errorCode.getCode(), key -> new ArrayList<>())
                    .add(((Enum<?>) errorCode).name());
        }

        Map<String, List<String>> duplicates = new LinkedHashMap<>();
        byCode.forEach((code, names) -> {
            if (names.size() > 1) {
                duplicates.put(code, names);
            }
        });

        assertThat(duplicates)
                .as("중복된 에러 코드")
                .isEmpty();
    }
}
