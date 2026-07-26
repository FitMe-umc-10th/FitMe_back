package umc.fitme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.exception.code.PostErrorCode;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.user.dto.SavedPostResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserSave;
import umc.fitme.domain.user.enums.SavedPostCategory;
import umc.fitme.domain.user.enums.SavedPostSort;
import umc.fitme.domain.user.exception.code.SavedPostErrorCode;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.domain.user.repository.UserSaveRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedPostServiceTest {

    @Mock
    private UserSaveRepository userSaveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private SavedPostService savedPostService;

    @Test
    @DisplayName("RECENT 정렬로 저장 공고 목록을 조회한다")
    void getSavedPosts_recent_success() {
        User user = User.builder().id(1L).build();

        Post post1 = Post.builder()
                .id(101L)
                .postType(PostType.CONTEST)
                .title("공모전 A")
                .organizer("기관 A")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 7, 20))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com/a")
                .imageUrl("https://example.com/a.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        Post post2 = Post.builder()
                .id(202L)
                .postType(PostType.CONTEST)
                .title("B")
                .organizer("기관 B")
                .applyStartAt(LocalDate.now())
                .applyEndAt(LocalDate.now().plusDays(2))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com/2")
                .createdAt(LocalDateTime.now())
                .build();

        Post post3 = Post.builder()
                .id(203L)
                .postType(PostType.CONTEST)
                .title("C")
                .organizer("기관 C")
                .applyStartAt(LocalDate.now())
                .applyEndAt(LocalDate.now().plusDays(3))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com/3")
                .createdAt(LocalDateTime.now())
                .build();

        UserSave save1 = UserSave.builder()
                .id(30L)
                .user(user)
                .post(post1)
                .isSaved(true)
                .build();

        UserSave save2 = UserSave.builder()
                .id(29L)
                .user(user)
                .post(post2)
                .isSaved(true)
                .build();

        UserSave save3 = UserSave.builder()
                .id(28L)
                .user(user)
                .post(post3)
                .isSaved(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findAllByUserAndIsSavedTrueOrderByIdDesc(eq(user), any()))
                .thenReturn(List.of(save1, save2, save3));

        SavedPostResponseDto.SavedPostListResponse result = savedPostService.getSavedPosts(1L, SavedPostCategory.ALL, SavedPostSort.RECENT, null, 2);

        assertEquals(2, result.savedPosts().size());
        assertTrue(result.pageInfo().hasNext());
        assertEquals("29", result.pageInfo().nextCursor());
    }

    @Test
    @DisplayName("size가 0 이하이면 예외가 발생한다")
    void getSavedPosts_invalidSize_fail() {
        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> savedPostService.getSavedPosts(1L, SavedPostCategory.ALL, SavedPostSort.RECENT, null, 0)
        );

        assertEquals(SavedPostErrorCode.INVALID_PAGE_SIZE, exception.getErrorCode());
    }

    @Test
    @DisplayName("잘못된 recent cursor면 예외가 발생한다")
    void getSavedPosts_invalidRecentCursor_fail() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> savedPostService.getSavedPosts(1L, SavedPostCategory.ALL, SavedPostSort.RECENT, "abc", 10)
        );

        assertEquals(SavedPostErrorCode.INVALID_CURSOR, exception.getErrorCode());
    }

    @Test
    @DisplayName("잘못된 deadline cursor면 예외가 발생한다")
    void getSavedPosts_invalidDeadlineCursor_fail() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> savedPostService.getSavedPosts(1L, SavedPostCategory.ALL, SavedPostSort.DEADLINE, "not-a-cursor", 10)
        );

        assertEquals(SavedPostErrorCode.INVALID_CURSOR, exception.getErrorCode());
    }

    @Test
    @DisplayName("DEADLINE 정렬로 저장 공고 목록을 조회하고 nextCursor를 반환한다")
    void getSavedPosts_deadline_success() {
        User user = User.builder().id(1L).build();

        Post post1 = Post.builder()
                .id(101L)
                .postType(PostType.CONTEST)
                .title("A")
                .organizer("기관 A")
                .applyStartAt(LocalDate.now())
                .applyEndAt(LocalDate.now().plusDays(1))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com/a")
                .createdAt(LocalDateTime.now())
                .build();

        Post post2 = Post.builder()
                .id(102L)
                .postType(PostType.CONTEST)
                .title("B")
                .organizer("기관 B")
                .applyStartAt(LocalDate.now())
                .applyEndAt(LocalDate.now().plusDays(2))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com/b")
                .createdAt(LocalDateTime.now())
                .build();

        UserSave save1 = UserSave.builder().id(1L).user(user).post(post1).isSaved(true).build();
        UserSave save2 = UserSave.builder().id(2L).user(user).post(post2).isSaved(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findAllByUserAndIsSavedTrueOrderByDeadlineAsc(eq(user), any()))
                .thenReturn(List.of(save1, save2));

        SavedPostResponseDto.SavedPostListResponse result =
                savedPostService.getSavedPosts(1L, SavedPostCategory.ALL, SavedPostSort.DEADLINE, null, 1);

        assertEquals(1, result.savedPosts().size());
        assertTrue(result.pageInfo().hasNext());
        assertEquals(post1.getApplyEndAt() + "_" + post1.getId(), result.pageInfo().nextCursor());
    }

    @Test
    @DisplayName("카테고리 필터 + 첫 페이지 조회 시 결과가 정상적으로 반환된다")
    void getSavedPosts_categoryFilter_firstPage_success() {
        User user = User.builder().id(1L).build();

        Post post = Post.builder()
                .id(101L)
                .postType(PostType.CONTEST)
                .title("공모전")
                .organizer("기관")
                .applyStartAt(LocalDate.now())
                .applyEndAt(LocalDate.now().plusDays(5))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com")
                .createdAt(LocalDateTime.now())
                .build();

        UserSave save = UserSave.builder().id(1L).user(user).post(post).isSaved(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findAllByUserAndIsSavedTrueAndPost_PostTypeOrderByIdDesc(eq(user), eq(PostType.CONTEST), any()))
                .thenReturn(List.of(save));

        SavedPostResponseDto.SavedPostListResponse result =
                savedPostService.getSavedPosts(1L, SavedPostCategory.CONTEST, SavedPostSort.RECENT, null, 10);

        assertEquals(1, result.savedPosts().size());
        assertFalse(result.pageInfo().hasNext());
    }

    @Test
    @DisplayName("카테고리 필터 + cursor로 다음 페이지를 조회한다")
    void getSavedPosts_categoryFilter_secondPage_success() {
        User user = User.builder().id(1L).build();

        Post post = Post.builder()
                .id(50L)
                .postType(PostType.SCHOLARSHIP)
                .title("장학금")
                .organizer("기관")
                .applyStartAt(LocalDate.now())
                .applyEndAt(LocalDate.now().plusDays(5))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com")
                .createdAt(LocalDateTime.now())
                .build();

        UserSave save = UserSave.builder().id(50L).user(user).post(post).isSaved(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findAllByUserAndIsSavedTrueAndPost_PostTypeAndIdLessThanOrderByIdDesc(
                eq(user), eq(PostType.SCHOLARSHIP), eq(60L), any()))
                .thenReturn(List.of(save));

        SavedPostResponseDto.SavedPostListResponse result =
                savedPostService.getSavedPosts(1L, SavedPostCategory.SCHOLARSHIP, SavedPostSort.RECENT, "60", 10);

        assertEquals(1, result.savedPosts().size());
        assertFalse(result.pageInfo().hasNext());
    }

    @Test
    @DisplayName("결과 수가 size와 정확히 같으면 hasNext는 false이다")
    void getSavedPosts_exactSize_hasNextFalse() {
        User user = User.builder().id(1L).build();

        Post post = Post.builder()
                .id(101L)
                .postType(PostType.CONTEST)
                .title("공모전")
                .organizer("기관")
                .applyStartAt(LocalDate.now())
                .applyEndAt(LocalDate.now().plusDays(5))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com")
                .createdAt(LocalDateTime.now())
                .build();

        UserSave save = UserSave.builder().id(1L).user(user).post(post).isSaved(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findAllByUserAndIsSavedTrueOrderByIdDesc(eq(user), any()))
                .thenReturn(List.of(save));

        SavedPostResponseDto.SavedPostListResponse result =
                savedPostService.getSavedPosts(1L, SavedPostCategory.ALL, SavedPostSort.RECENT, null, 1);

        assertEquals(1, result.savedPosts().size());
        assertFalse(result.pageInfo().hasNext());
        assertNull(result.pageInfo().nextCursor());
    }

    @Test
    @DisplayName("저장한 공고가 없으면 빈 목록을 반환한다")
    void getSavedPosts_empty_success() {
        User user = User.builder().id(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findAllByUserAndIsSavedTrueOrderByIdDesc(eq(user), any()))
                .thenReturn(List.of());

        SavedPostResponseDto.SavedPostListResponse result =
                savedPostService.getSavedPosts(1L, SavedPostCategory.ALL, SavedPostSort.RECENT, null, 10);

        assertTrue(result.savedPosts().isEmpty());
        assertFalse(result.pageInfo().hasNext());
        assertNull(result.pageInfo().nextCursor());
    }

    @Test
    @DisplayName("공고 찜하기 성공")
    void savePost_success() {
        User user = User.builder()
                .id(1L)
                .build();

        Post post = Post.builder()
                .id(10L)
                .postType(PostType.CONTEST)
                .title("공모전")
                .organizer("주최기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 7, 31))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com")
                .imageUrl("https://example.com/thumb.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        UserSave savedUserSave = UserSave.builder()
                .id(100L)
                .user(user)
                .post(post)
                .isSaved(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userSaveRepository.findByUserAndPost(user, post)).thenReturn(Optional.empty());
        when(userSaveRepository.save(any(UserSave.class))).thenReturn(savedUserSave);

        SavedPostResponseDto.SavePostResponse response = savedPostService.savePost(1L, 10L);

        assertNotNull(response);
        assertEquals(100L, response.savedId());
        assertEquals(10L, response.postId());
        assertTrue(response.saved());
    }

    @Test
    @DisplayName("이미 찜한 공고면 예외 발생")
    void savePost_alreadySaved() {
        User user = User.builder()
                .id(1L)
                .build();

        Post post = Post.builder()
                .id(10L)
                .postType(PostType.CONTEST)
                .title("공모전")
                .organizer("주최기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 7, 31))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com")
                .imageUrl("https://example.com/thumb.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        UserSave existingUserSave = UserSave.builder()
                .id(99L)
                .user(user)
                .post(post)
                .isSaved(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userSaveRepository.findByUserAndPost(user, post)).thenReturn(Optional.of(existingUserSave));

        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> savedPostService.savePost(1L, 10L)
        );

        assertEquals(SavedPostErrorCode.ALREADY_SAVED_POST, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 공고 저장 시 예외 발생")
    void savePost_postNotFound() {
        User user = User.builder()
                .id(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(10L)).thenReturn(Optional.empty());

        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> savedPostService.savePost(1L, 10L)
        );

        assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("저장 공고 취소 성공")
    void deleteSavedPost_success() {
        User user = User.builder()
                .id(1L)
                .build();

        Post post = Post.builder()
                .id(10L)
                .postType(PostType.CONTEST)
                .title("공모전")
                .organizer("주최기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 7, 31))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com")
                .imageUrl("https://example.com/thumb.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        UserSave userSave = UserSave.builder()
                .id(100L)
                .user(user)
                .post(post)
                .isSaved(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(userSave));

        SavedPostResponseDto.DeleteSavedPostResponse response = savedPostService.deleteSavedPost(1L, 100L);

        assertNotNull(response);
        assertEquals(100L, response.savedId());
        assertEquals(10L, response.postId());
        assertFalse(response.saved());
        assertFalse(userSave.getIsSaved());
    }

    @Test
    @DisplayName("저장 공고가 없으면 예외 발생")
    void deleteSavedPost_notFound() {
        User user = User.builder()
                .id(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> savedPostService.deleteSavedPost(1L, 100L)
        );

        assertEquals(SavedPostErrorCode.SAVED_POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 저장 취소된 공고면 예외 발생")
    void deleteSavedPost_alreadyUnsaved() {
        User user = User.builder()
                .id(1L)
                .build();

        Post post = Post.builder()
                .id(10L)
                .postType(PostType.CONTEST)
                .title("공모전")
                .organizer("주최기관")
                .applyStartAt(LocalDate.of(2026, 7, 1))
                .applyEndAt(LocalDate.of(2026, 7, 31))
                .applicationMethod("온라인")
                .applicationUrl("https://example.com")
                .imageUrl("https://example.com/thumb.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        UserSave userSave = UserSave.builder()
                .id(100L)
                .user(user)
                .post(post)
                .isSaved(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(userSave));

        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> savedPostService.deleteSavedPost(1L, 100L)
        );

        assertEquals(SavedPostErrorCode.ALREADY_UNSAVED_POST, exception.getErrorCode());
    }
}