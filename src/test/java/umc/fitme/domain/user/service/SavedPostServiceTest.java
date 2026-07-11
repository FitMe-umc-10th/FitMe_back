package umc.fitme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.dto.SavedPostResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserSave;
import umc.fitme.domain.user.enums.SavedPostCategory;
import umc.fitme.domain.user.enums.SavedPostSort;
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
                .createdAt(LocalDateTime.now())
                .build();

        UserSave save2 = UserSave.builder()
                .id(29L)
                .user(user)
                .post(post2)
                .isSaved(true)
                .createdAt(LocalDateTime.now())
                .build();

        UserSave save3 = UserSave.builder()
                .id(28L)
                .user(user)
                .post(post3)
                .isSaved(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSaveRepository.findAllByUserAndIsSavedTrueOrderByIdDesc(eq(user), any()))
                .thenReturn(List.of(save1, save2, save3));

        SavedPostResponseDto.SavedPostListResponse result = savedPostService.getSavedPosts(SavedPostCategory.ALL, SavedPostSort.RECENT, null, 2);

        assertEquals(2, result.savedPosts().size());
        assertTrue(result.pageInfo().hasNext());
        assertEquals("29", result.pageInfo().nextCursor());
    }

    @Test
    @DisplayName("size가 0 이하이면 예외가 발생한다")
    void getSavedPosts_invalidSize_fail() {
        assertThrows(ProjectException.class, () -> savedPostService.getSavedPosts(SavedPostCategory.ALL, SavedPostSort.RECENT, null, 0));
    }

    @Test
    @DisplayName("잘못된 recent cursor면 예외가 발생한다")
    void getSavedPosts_invalidRecentCursor_fail() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ProjectException.class, () -> savedPostService.getSavedPosts(SavedPostCategory.ALL, SavedPostSort.RECENT, "abc", 10));
    }
}