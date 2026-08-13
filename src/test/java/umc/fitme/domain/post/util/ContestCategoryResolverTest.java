package umc.fitme.domain.post.util;

import org.junit.jupiter.api.Test;
import umc.fitme.domain.post.enums.ContestCategory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 온보딩 관심 분야가 자유 문자열이라 태그 선택과 직접 입력이 섞여 들어오는 것을 전제로 한다.
 */
class ContestCategoryResolverTest {

    private final ContestCategoryResolver resolver = new ContestCategoryResolver();

    @Test
    void 온보딩_태그_6개를_카테고리로_바꾼다() {
        // 화면 설계서의 관심 분야 태그 그대로다.
        assertThat(resolver.resolve("마케팅")).isEqualTo(ContestCategory.MARKETING);
        assertThat(resolver.resolve("기획/아이디어")).isEqualTo(ContestCategory.PM);
        assertThat(resolver.resolve("디자인")).isEqualTo(ContestCategory.DESIGN);
        assertThat(resolver.resolve("IT/개발")).isEqualTo(ContestCategory.IT);
        assertThat(resolver.resolve("영상편집")).isEqualTo(ContestCategory.VIDEO);
    }

    @Test
    void 어학은_대응하는_카테고리가_없어_판정하지_않는다() {
        // ContestCategory에 어학이 없다. 어학 공모전은 ETC로 분류되어 어차피 항상 노출된다.
        assertThat(resolver.resolve("어학")).isNull();
    }

    @Test
    void 공백과_대소문자가_달라도_찾는다() {
        assertThat(resolver.resolve(" IT/개발 ")).isEqualTo(ContestCategory.IT);
        assertThat(resolver.resolve("it/개발")).isEqualTo(ContestCategory.IT);
        assertThat(resolver.resolve("ux/ui 디자인")).isEqualTo(ContestCategory.DESIGN);
    }

    @Test
    void 직접_입력은_보조_키워드로_찾는다() {
        assertThat(resolver.resolve("AI")).isEqualTo(ContestCategory.IT);
        assertThat(resolver.resolve("영상")).isEqualTo(ContestCategory.VIDEO);
        assertThat(resolver.resolve("기타")).isEqualTo(ContestCategory.ETC);
    }

    @Test
    void 알아볼_수_없는_직접입력은_null이다() {
        assertThat(resolver.resolve("로봇공학")).isNull();
        assertThat(resolver.resolve("")).isNull();
        assertThat(resolver.resolve(null)).isNull();
    }

    @Test
    void 여러_관심분야를_집합으로_바꾼다() {
        assertThat(resolver.resolveAll(Set.of("IT/개발", "디자인")))
                .containsExactlyInAnyOrder(ContestCategory.IT, ContestCategory.DESIGN);
    }

    @Test
    void 어학과_다른_태그를_함께_고르면_다른_태그로_판정한다() {
        // 어학 때문에 전체가 통과로 열리면 안 된다.
        assertThat(resolver.resolveAll(Set.of("어학", "디자인")))
                .containsExactly(ContestCategory.DESIGN);
    }

    @Test
    void 알아본_것만_집합에_남는다() {
        // 직접 입력한 '로봇공학'은 빠지고 '디자인'만 남는다.
        assertThat(resolver.resolveAll(Set.of("디자인", "로봇공학")))
                .containsExactly(ContestCategory.DESIGN);
        assertThat(resolver.resolveAll(Set.of("로봇공학"))).isEmpty();
        assertThat(resolver.resolveAll(Set.of())).isEmpty();
        assertThat(resolver.resolveAll(null)).isEmpty();
    }
}
