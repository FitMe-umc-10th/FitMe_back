package umc.fitme.domain.post.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * resources/data/university.csv를 실제로 읽어 검증한다.
 * 온보딩이 자유 입력이라 약칭·공백·캠퍼스 표기가 섞여 들어오는 것을 전제로 한다.
 */
class UniversityTypeResolverTest {

    private final UniversityTypeResolver resolver = new UniversityTypeResolver();

    @Test
    void 대학_목록을_모두_읽는다() {
        // 원본 418행에서 캠퍼스 표기를 지우면 406개 학교로 합쳐진다.
        assertThat(resolver.size()).isEqualTo(406);
    }

    @Test
    void 정식_명칭으로_구분을_찾는다() {
        assertThat(resolver.resolveType("가천대학교")).isEqualTo("4년제");
        assertThat(resolver.resolveType("ICT폴리텍대학")).isEqualTo("전문대");
    }

    @Test
    void 약칭과_공백이_섞여도_구분을_찾는다() {
        assertThat(resolver.resolveType("가천대")).isEqualTo("4년제");
        assertThat(resolver.resolveType("가천대학")).isEqualTo("4년제");
        assertThat(resolver.resolveType(" 가천대학교 ")).isEqualTo("4년제");
    }

    @Test
    void 캠퍼스_표기가_붙어도_구분을_찾는다() {
        assertThat(resolver.resolveType("가야대학교(고령)")).isEqualTo("4년제");
        assertThat(resolver.resolveType("가야대학교(김해)")).isEqualTo("4년제");
    }

    @Test
    void 목록에_없는_학교는_null을_돌려준다() {
        assertThat(resolver.resolveType("듣보잡대학교")).isNull();
        assertThat(resolver.resolveType("")).isNull();
        assertThat(resolver.resolveType(null)).isNull();
    }

    @Test
    void 구분_키워드가_들어간_조건을_알아본다() {
        assertThat(resolver.containsTypeKeyword("4년제(5~6년제포함)전문대(2~3년제)")).isTrue();
        assertThat(resolver.containsTypeKeyword("4년제(5~6년제포함)")).isTrue();
        assertThat(resolver.containsTypeKeyword(
                "4년제(5~6년제포함)기술대학원격대학전문대(2~3년제)학점은행제 대학해외대학")).isTrue();
        // 국내 구분이 하나도 없어도 판정 가능한 조건이다. 국내 재학생은 여기서 걸러진다.
        assertThat(resolver.containsTypeKeyword("해외대학")).isTrue();
    }

    @Test
    void 문장형_조건에는_구분_키워드가_없다() {
        assertThat(resolver.containsTypeKeyword("도내 대학 해외교환 장학생")).isFalse();
        assertThat(resolver.containsTypeKeyword("해당없음")).isFalse();
        assertThat(resolver.containsTypeKeyword("")).isFalse();
        assertThat(resolver.containsTypeKeyword(null)).isFalse();
    }
}
