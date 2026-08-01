package umc.fitme.domain.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserSave;
import umc.fitme.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link UserSaveRepository#findByIdAndUserAndIsSavedTrue}가 isSaved=false 레코드를
 * 실제 쿼리 레벨에서 제외하는지 검증한다. (Service 테스트는 이 조회 결과를 mock하므로
 * 파생 쿼리 자체의 동작은 별도로 검증되지 않았다.)
 */
class UserSaveRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private UserSaveRepository userSaveRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("isSaved=false인 레코드는 조회되지 않는다")
    void findByIdAndUserAndIsSavedTrue_excludesUnsaved() {
        // given
        User user = persistUser("unsaved@test.com");
        Post post = persistPost();

        UserSave unsaved = UserSave.builder()
                .user(user)
                .post(post)
                .isSaved(false)
                .build();
        em.persist(unsaved);
        flushAndClear();

        // when
        Optional<UserSave> result =
                userSaveRepository.findByIdAndUserAndIsSavedTrue(unsaved.getId(), user);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("isSaved=true인 레코드는 정상적으로 조회된다")
    void findByIdAndUserAndIsSavedTrue_returnsSaved() {
        // given
        User user = persistUser("saved@test.com");
        Post post = persistPost();

        UserSave saved = UserSave.builder()
                .user(user)
                .post(post)
                .isSaved(true)
                .build();
        em.persist(saved);
        flushAndClear();

        // when
        Optional<UserSave> result =
                userSaveRepository.findByIdAndUserAndIsSavedTrue(saved.getId(), user);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("다른 사용자의 저장 기록은 조회되지 않는다")
    void findByIdAndUserAndIsSavedTrue_excludesOtherUser() {
        // given
        User owner = persistUser("owner@test.com");
        User stranger = persistUser("stranger@test.com");
        Post post = persistPost();

        UserSave saved = UserSave.builder()
                .user(owner)
                .post(post)
                .isSaved(true)
                .build();
        em.persist(saved);
        flushAndClear();

        // when
        Optional<UserSave> result =
                userSaveRepository.findByIdAndUserAndIsSavedTrue(saved.getId(), stranger);

        // then
        assertThat(result).isEmpty();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private User persistUser(String email) {
        User user = User.builder()
                .email(email)
                .name("테스터")
                .build();
        return em.persist(user);
    }

    private Post persistPost() {
        Post post = Post.builder()
                .postType(PostType.CONTEST)
                .title("테스트 공모전")
                .organizer("테스트 주최")
                .applyStartAt(LocalDate.of(2026, 1, 1))
                .applyEndAt(LocalDate.of(2026, 12, 31))
                .applicationMethod("온라인 접수")
                .applicationUrl("https://example.com")
                .imageUrl("https://example.com/image.png")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
        return em.persist(post);
    }
}
