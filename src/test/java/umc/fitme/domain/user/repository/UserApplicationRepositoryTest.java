package umc.fitme.domain.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;
import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.enums.Status;
import umc.fitme.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link UserApplicationRepository#sumFinalPassedScholarshipAmount} 및 count 쿼리 검증.
 * <p>
 * DB 설정(@DataJpaTest, 실제 MySQL fitme_test)은 {@link RepositoryTestSupport}에서 물려받는다.
 * Scholarship/Contest는 빌더·세터가 없으므로 {@link ReflectionTestUtils}로 필드를 심는다.
 * 장학금 금액은 저장 시 @PrePersist가 supportAmount(String)를 파싱해 supportAmountValue(Long)에 넣는다.
 */
class UserApplicationRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private UserApplicationRepository userApplicationRepository;

    @Autowired
    private TestEntityManager em;

    @Nested
    @DisplayName("sumFinalPassedScholarshipAmount")
    class SumFinalPassedScholarshipAmount {

        @Test
        @DisplayName("FINAL_PASSED 상태 장학금들의 supportAmountValue 합계를 반환한다")
        void 최종합격_금액_합계를_반환한다() {
            // given: 100만원(1,000,000) + 200만원(2,000,000)
            User user = persistUser("final@test.com");
            persistApplication(user, persistScholarship("100만원"), Status.FINAL_PASSED);
            persistApplication(user, persistScholarship("200만원"), Status.FINAL_PASSED);
            flushAndClear();

            // when
            long total = userApplicationRepository
                    .sumFinalPassedScholarshipAmount(user, Status.FINAL_PASSED);

            // then
            assertThat(total).isEqualTo(3_000_000L);
        }

        @Test
        @DisplayName("FINAL_PASSED 외 다른 상태(DOCUMENT_PASSED, PENDING_RESULT, NONE)는 합계에서 제외된다")
        void 다른_상태는_합계에서_제외된다() {
            // given: FINAL_PASSED만 합산 대상
            User user = persistUser("mixed@test.com");
            persistApplication(user, persistScholarship("300만원"), Status.FINAL_PASSED);
            persistApplication(user, persistScholarship("999만원"), Status.DOCUMENT_PASSED);
            persistApplication(user, persistScholarship("999만원"), Status.PENDING_RESULT);
            persistApplication(user, persistScholarship("999만원"), Status.NONE);
            flushAndClear();

            // when
            long total = userApplicationRepository
                    .sumFinalPassedScholarshipAmount(user, Status.FINAL_PASSED);

            // then
            assertThat(total).isEqualTo(3_000_000L);
        }

        @Test
        @DisplayName("지원 이력이 없으면 0을 반환한다")
        void 지원이력이_없으면_0() {
            // given
            User user = persistUser("empty@test.com");
            flushAndClear();

            // when
            long total = userApplicationRepository
                    .sumFinalPassedScholarshipAmount(user, Status.FINAL_PASSED);

            // then
            assertThat(total).isZero();
        }

        @Test
        @DisplayName("Scholarship-Post id 상속 조인이 실제로 물려 금액이 합산되고, 장학금이 아닌 게시글(Contest)은 제외된다")
        void 상속조인이_실제로_동작한다() {
            // given: 같은 post 테이블(SINGLE_TABLE)에 장학금과 공모전이 섞여 있어도
            //        JOIN Scholarship s ON s.id = ua.post.id 는 dtype=scholarship 행만 매칭되어야 한다.
            User user = persistUser("join@test.com");
            persistApplication(user, persistScholarship("500만원"), Status.FINAL_PASSED);
            persistApplication(user, persistContest(), Status.FINAL_PASSED);
            flushAndClear();

            // when
            long total = userApplicationRepository
                    .sumFinalPassedScholarshipAmount(user, Status.FINAL_PASSED);

            // then: 공모전(Contest)은 상속 조인에서 걸러지고 장학금 금액만 합산된다.
            assertThat(total).isEqualTo(5_000_000L);
        }
    }

    // 지원 "완료"로 간주하는 상태 집합 (MyPageService 기준)
    private static final List<Status> COMPLETED_STATUSES =
            List.of(Status.PENDING_RESULT, Status.DOCUMENT_PASSED, Status.FINAL_PASSED);

    @Nested
    @DisplayName("countByUserAndStatusIn (완료 건수)")
    class CountByUserAndStatusIn {

        @Test
        @DisplayName("여러 상태가 섞여 있어도 완료 집합에 속한 건수만 정확히 카운트된다")
        void 완료집합만_정확히_카운트된다() {
            // given: 완료 집합 3건 + 완료가 아닌 NONE 1건
            User user = persistUser("completed@test.com");
            persistApplication(user, persistScholarship("장학금1"), Status.PENDING_RESULT);
            persistApplication(user, persistScholarship("장학금2"), Status.DOCUMENT_PASSED);
            persistApplication(user, persistScholarship("장학금3"), Status.FINAL_PASSED);
            persistApplication(user, persistScholarship("장학금4"), Status.NONE);
            flushAndClear();

            // when
            long count = userApplicationRepository.countByUserAndStatusIn(user, COMPLETED_STATUSES);

            // then
            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("NONE 상태만 있으면 완료 집합 카운트는 0이다 (경계 고정)")
        void NONE_상태는_완료집합에서_제외된다() {
            // given: 완료 집합 경계 바로 밖인 NONE 만 존재
            User user = persistUser("none-only@test.com");
            persistApplication(user, persistScholarship("장학금1"), Status.NONE);
            persistApplication(user, persistScholarship("장학금2"), Status.NONE);
            flushAndClear();

            // when
            long count = userApplicationRepository.countByUserAndStatusIn(user, COMPLETED_STATUSES);

            // then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("countByUserAndStatus (대기 건수)")
    class CountByUserAndStatus {

        @Test
        @DisplayName("여러 상태가 섞여 있어도 PENDING_RESULT 건수만 대기로 카운트된다")
        void PENDING_RESULT만_대기로_카운트된다() {
            // given: PENDING_RESULT 2건 + 다른 상태들
            User user = persistUser("pending@test.com");
            persistApplication(user, persistScholarship("장학금1"), Status.PENDING_RESULT);
            persistApplication(user, persistScholarship("장학금2"), Status.PENDING_RESULT);
            persistApplication(user, persistScholarship("장학금3"), Status.DOCUMENT_PASSED);
            persistApplication(user, persistScholarship("장학금4"), Status.FINAL_PASSED);
            persistApplication(user, persistScholarship("장학금5"), Status.NONE);
            flushAndClear();

            // when
            long count = userApplicationRepository.countByUserAndStatus(user, Status.PENDING_RESULT);

            // then
            assertThat(count).isEqualTo(2L);
        }
    }

    // --- 테스트 데이터 헬퍼 -------------------------------------------------

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

    private UserApplication persistApplication(User user, Post post, Status status) {
        UserApplication application = UserApplication.builder()
                .user(user)
                .post(post)
                .status(status)
                .build();
        return em.persist(application);
    }

    private Scholarship persistScholarship(String supportAmount) {
        Scholarship scholarship = new Scholarship();
        setPostFields(scholarship, PostType.SCHOLARSHIP, "테스트 장학금");
        ReflectionTestUtils.setField(scholarship, "gradeRequirement", "제한없음");
        ReflectionTestUtils.setField(scholarship, "incomeRequirement", "제한없음");
        ReflectionTestUtils.setField(scholarship, "regionRequirement", "전국");
        ReflectionTestUtils.setField(scholarship, "supportAmount", supportAmount);
        return em.persist(scholarship);
    }

    private Contest persistContest() {
        Contest contest = new Contest();
        setPostFields(contest, PostType.CONTEST, "테스트 공모전");
        ReflectionTestUtils.setField(contest, "posterImageUrl", "https://example.com/poster.png");
        ReflectionTestUtils.setField(contest, "target", "대학생");
        ReflectionTestUtils.setField(contest, "participantLimit", "제한없음");
        ReflectionTestUtils.setField(contest, "rewardTotal", "1000만원");
        return em.persist(contest);
    }

    /**
     * Post는 빌더·세터가 서브타입에 상속되지 않으므로 nullable=false 인 공통 필드를 리플렉션으로 채운다.
     */
    private void setPostFields(Post post, PostType postType, String title) {
        ReflectionTestUtils.setField(post, "postType", postType);
        ReflectionTestUtils.setField(post, "title", title);
        ReflectionTestUtils.setField(post, "organizer", "테스트 주최");
        ReflectionTestUtils.setField(post, "applyStartAt", LocalDate.of(2026, 1, 1));
        ReflectionTestUtils.setField(post, "applyEndAt", LocalDate.of(2026, 12, 31));
        ReflectionTestUtils.setField(post, "applicationMethod", "온라인 접수");
        ReflectionTestUtils.setField(post, "applicationUrl", "https://example.com");
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
    }
}