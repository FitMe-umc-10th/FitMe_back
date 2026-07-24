package umc.fitme.domain.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import umc.fitme.domain.user.entity.User;
import umc.fitme.support.RepositoryTestSupport;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link UserRepository#findByIdAndDeletedAtIsNull} 검증.
 * <p>
 * DB 설정(@DataJpaTest, 실제 MySQL fitme_test)은 {@link RepositoryTestSupport}에서 물려받는다.
 * deletedAt 은 @Builder 로 직접 주입한다(User 에 softDelete 메서드가 없음).
 */
class UserRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    @Nested
    @DisplayName("findByIdAndDeletedAtIsNull")
    class FindByIdAndDeletedAtIsNull {

        @Test
        @DisplayName("deletedAt 이 null 인 유저는 정상 조회된다")
        void 활성_유저_조회() {
            // given
            User user = persistUser("active@test.com", null);
            flushAndClear();

            // when
            Optional<User> found = userRepository.findByIdAndDeletedAtIsNull(user.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("active@test.com");
        }

        @Test
        @DisplayName("deletedAt 이 채워진(탈퇴) 유저는 조회되지 않는다")
        void 탈퇴_유저_제외() {
            // given
            User user = persistUser("deleted@test.com", LocalDateTime.of(2026, 1, 1, 0, 0));
            flushAndClear();

            // when
            Optional<User> found = userRepository.findByIdAndDeletedAtIsNull(user.getId());

            // then
            assertThat(found).isEmpty();
        }
    }

    private User persistUser(String email, LocalDateTime deletedAt) {
        User user = User.builder()
                .email(email)
                .name("테스터")
                .deletedAt(deletedAt)
                .build();
        return em.persist(user);
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}