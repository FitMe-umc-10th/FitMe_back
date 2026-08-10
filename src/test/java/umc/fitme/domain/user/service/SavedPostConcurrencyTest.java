package umc.fitme.domain.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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
import java.util.ArrayList;
import java.util.List;
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

    private final List<Long> userIds = new ArrayList<>();
    private Long postId;

    @AfterEach
    void cleanUp() {
        for (Long id : userIds) {
            userRepository.findById(id).ifPresent(user -> {
                userSaveRepository.deleteAll(
                        userSaveRepository.findAllByUserAndIsSavedTrueOrderByIdDesc(
                                user,
                                PageRequest.of(0, 100)
                        )
                );
                userRepository.deleteById(user.getId());
            });
        }
        userIds.clear();

        if (postId != null) {
            postRepository.deleteById(postId);
            postId = null;
        }
    }

    private User createUser(String email) {
        User user = userRepository.save(
                User.builder().email(email).name("동시성테스트유저").build()
        );
        userIds.add(user.getId());
        return user;
    }

    private Post createPost() {
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
        postId = post.getId();
        return post;
    }

    @Test
    @DisplayName("동시에 같은 공고를 저장 요청하면 정확히 한 요청만 성공하고 나머지는 409로 실패한다")
    void savePost_concurrentRequests_onlyOneSucceeds() throws InterruptedException {
        // given
        User user = createUser("concurrency-test@fitme.com");
        Post post = createPost();
        Long userId = user.getId();

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
                .findAllByUserAndIsSavedTrueOrderByIdDesc(user, PageRequest.of(0, 100))
                .size();
        assertThat(savedRowCount).isEqualTo(1);

        // 성공한 요청이 하나뿐이므로 찜 수도 정확히 1이어야 한다.
        assertThat(postRepository.findById(postId).orElseThrow().getSavedCount()).isEqualTo(1);
    }

    /**
     * UserSave 행에 건 비관적 락은 "같은 (user, post)" 경합만 막는다.
     * 서로 다른 유저가 같은 공고를 찜하면 잠기는 user_save 행이 서로 달라 락이 충돌하지 않으므로,
     * savedCount를 엔티티에서 읽고 더하면 증가분이 유실된다.
     * 원자적 UPDATE(PostRepository.increaseSavedCount)로 이 손실이 사라졌는지 검증한다.
     */
    @Test
    @DisplayName("서로 다른 유저가 같은 공고를 동시에 찜하면 찜 수가 유실 없이 모두 반영된다")
    void savePost_differentUsersSamePost_noLostUpdate() throws InterruptedException {
        // given
        int threadCount = 20;
        Post post = createPost();
        List<Long> savers = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            savers.add(createUser("concurrency-multi-" + i + "@fitme.com").getId());
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        // when: 서로 다른 유저가 동시에 같은 공고를 찜한다
        for (Long saverId : savers) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    savedPostService.savePost(saverId, postId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // then: 모든 요청이 서로 다른 (user, post)라 전부 성공해야 한다
        assertThat(finished).isTrue();
        assertThat(failureCount.get()).isZero();
        assertThat(successCount.get()).isEqualTo(threadCount);

        // 핵심: 갱신 손실이 없다면 savedCount는 정확히 threadCount다
        assertThat(postRepository.findById(post.getId()).orElseThrow().getSavedCount())
                .isEqualTo(threadCount);
    }
}
