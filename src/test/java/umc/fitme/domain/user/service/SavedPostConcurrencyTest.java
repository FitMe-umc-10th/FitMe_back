package umc.fitme.domain.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.exception.code.SavedPostErrorCode;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.domain.user.repository.UserSaveRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * findByUserAndPost() 조회와 saveAndFlush() 사이에는 원자성이 없다.
 * 동시에 같은 (user, post)로 저장 요청이 들어와도 정확히 하나만 성공하고
 * 나머지는 ALREADY_SAVED_POST(409)로 실패해야 함을, 실제 DB 유니크 제약을 통해 검증한다.
 * (Mockito로는 진짜 동시성/유니크 제약 충돌을 재현할 수 없어 실제 DB를 쓰는 통합 테스트로 작성)
 */
@SpringBootTest
@ActiveProfiles("test")
class SavedPostConcurrencyTest {

    @Autowired
    private SavedPostService savedPostService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserSaveRepository userSaveRepository;

    private Long userId;
    private Long postId;

    @AfterEach
    void cleanUp() {
        if (userId != null) {
            userSaveRepository.deleteAll(
                    userSaveRepository.findAllByUserAndIsSavedTrueOrderByIdDesc(
                            userRepository.findById(userId).orElseThrow(),
                            org.springframework.data.domain.PageRequest.of(0, 100)
                    )
            );
            userRepository.deleteById(userId);
        }
        if (postId != null) {
            postRepository.deleteById(postId);
        }
    }

    @Test
    @DisplayName("동시에 같은 공고를 저장 요청하면 정확히 한 요청만 성공하고 나머지는 409로 실패한다")
    void savePost_concurrentRequests_onlyOneSucceeds() throws InterruptedException {
        // given
        User user = userRepository.save(
                User.builder().email("concurrency-test@fitme.com").name("동시성테스트유저").build()
        );
        Post post = postRepository.save(
                Post.builder()
                        .postType(PostType.CONTEST)
                        .title("동시성 테스트 공모전")
                        .organizer("테스트 기관")
                        .applyStartAt(LocalDate.of(2026, 1, 1))
                        .applyEndAt(LocalDate.of(2026, 12, 31))
                        .applicationMethod("온라인")
                        .applicationUrl("https://example.com")
                        .imageUrl("https://example.com/thumb.jpg")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        userId = user.getId();
        postId = post.getId();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();
        AtomicInteger unexpectedCount = new AtomicInteger();

        // when: threadCount개의 요청이 동시에 같은 (user, post)를 저장 시도
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    savedPostService.savePost(userId, postId);
                    successCount.incrementAndGet();
                } catch (ProjectException e) {
                    if (e.getErrorCode() == SavedPostErrorCode.ALREADY_SAVED_POST) {
                        conflictCount.incrementAndGet();
                    } else {
                        unexpectedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    unexpectedCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(finished).isTrue();
        assertThat(unexpectedCount.get()).isZero();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(threadCount - 1);

        long savedRowCount = userSaveRepository
                .findAllByUserAndIsSavedTrueOrderByIdDesc(user, org.springframework.data.domain.PageRequest.of(0, 100))
                .size();
        assertThat(savedRowCount).isEqualTo(1);
    }
}
