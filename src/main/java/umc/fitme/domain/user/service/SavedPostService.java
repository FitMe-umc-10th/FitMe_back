package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.user.converter.SavedPostConverter;
import umc.fitme.domain.user.dto.SavedPostResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserSave;
import umc.fitme.domain.user.enums.SavedPostCategory;
import umc.fitme.domain.user.enums.SavedPostSort;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.domain.user.repository.UserSaveRepository;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.LocalDate;
import java.util.List;

import static umc.fitme.domain.user.converter.SavedPostConverter.convertCategory;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedPostService {

    private static final int MAX_SIZE = 100;

    private final UserSaveRepository userSaveRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public SavedPostResponseDto.SavedPostListResponse getSavedPosts(
            Long userId,
            SavedPostCategory category,
            SavedPostSort sort,
            String cursor,
            Integer size
    ) {
        validateSize(size);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_NOT_FOUND));


        List<UserSave> savedPosts = switch (sort) {
            case RECENT -> getRecentSavedPosts(user, category, cursor, size + 1);
            case DEADLINE -> getDeadlineSavedPosts(user, category, cursor, size + 1);
        };

        boolean hasNext = savedPosts.size() > size;
        if (hasNext) {
            savedPosts = savedPosts.subList(0, size);
        }

        String nextCursor = null;
        if (hasNext && !savedPosts.isEmpty()) {
            UserSave last = savedPosts.get(savedPosts.size() - 1);
            nextCursor = createNextCursor(sort, last);
        }

        List<SavedPostResponseDto.SavedPostItem> items =
                SavedPostConverter.toSavedPostItemList(savedPosts);

        return SavedPostConverter.toSavedPostListResponse(
                SavedPostConverter.toSavedPostItemList(savedPosts),
                nextCursor,
                size,
                hasNext
        );
    }

    @Transactional
    public SavedPostResponseDto.SavePostResponse savePost(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.POST_NOT_FOUND));

        userSaveRepository.findByUserAndPost(user,post)
                .ifPresent(userSave -> {
                    throw new ProjectException(GeneralErrorCode.ALREADY_SAVED_POST);
                });

        UserSave userSave = UserSave.builder()
                .user(user)
                .post(post)
                .isSaved(true)
                .build();

        UserSave saved = userSaveRepository.save(userSave);

        return SavedPostConverter.toSavePostResponse(saved);
    }

    private void validateSize(Integer size) {
        if (size == null || size <= 0 || size > MAX_SIZE) {
            throw new ProjectException(GeneralErrorCode.INVALID_PAGE_SIZE);
        }
    }

    private List<UserSave> getRecentSavedPosts(
            User user,
            SavedPostCategory category,
            String cursor,
            int size
    ) {
        Long savedIdCursor = parseRecentCursor(cursor);
        Pageable pageable = PageRequest.of(0, size);

        if (category == SavedPostCategory.ALL) {
            if (savedIdCursor == null) {
                return userSaveRepository.findAllByUserAndIsSavedTrueOrderByIdDesc(user, pageable);
            }
            return userSaveRepository.findAllByUserAndIsSavedTrueAndIdLessThanOrderByIdDesc(
                    user, savedIdCursor, pageable
            );
        }

        return userSaveRepository.findAllByUserAndIsSavedTrueAndPost_PostTypeAndIdLessThanOrderByIdDesc(
                user, convertCategory(category), savedIdCursor, pageable
        );
    }

    private List<UserSave> getDeadlineSavedPosts(
            User user,
            SavedPostCategory category,
            String cursor,
            int size
    ) {
        Pageable pageable = PageRequest.of(0, size + 1);
        DeadlineCursor deadlineCursor = parseDeadlineCursor(cursor);

        if (category == SavedPostCategory.ALL) {
            return deadlineCursor == null
                    ? userSaveRepository.findAllByUserAndIsSavedTrueOrderByDeadlineAsc(user, pageable)
                    : userSaveRepository.findAllByUserAndIsSavedTrueAndDeadlineCursorOrderByDeadlineAsc(
                    user,
                    deadlineCursor.deadlineDate(),
                    deadlineCursor.postId(),
                    pageable
            );
        }

        PostType postType = convertCategory(category);

        return deadlineCursor == null
                ? userSaveRepository.findAllByUserAndIsSavedTrueAndPostCategoryOrderByDeadlineAsc(
                user,
                postType,
                pageable
        )
                : userSaveRepository.findAllByUserAndIsSavedTrueAndPostCategoryAndDeadlineCursorOrderByDeadlineAsc(
                user,
                postType,
                deadlineCursor.deadlineDate(),
                deadlineCursor.postId(),
                pageable
        );
    }

    private Long parseRecentCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            throw new ProjectException(GeneralErrorCode.INVALID_CURSOR);
        }
    }

    private DeadlineCursor parseDeadlineCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String[] parts = cursor.split("_");
            if (parts.length != 2) {
                throw new IllegalArgumentException();
            }

            LocalDate deadlineDate = LocalDate.parse(parts[0]);
            Long postId = Long.parseLong(parts[1]);

            return new DeadlineCursor(deadlineDate, postId);
        } catch (Exception e) {
            throw new ProjectException(GeneralErrorCode.INVALID_CURSOR);
        }
    }

    private String createNextCursor(SavedPostSort sort, UserSave savedPost) {
        if (sort == SavedPostSort.RECENT) {
            return String.valueOf(savedPost.getId());
        }

        Post post = savedPost.getPost();
        return post.getApplyEndAt() + "_" + post.getId();
    }

    private record DeadlineCursor(
            LocalDate deadlineDate,
            Long postId
    ) {

    }

    @Transactional
    public SavedPostResponseDto.DeleteSavedPostResponse deleteSavedPost(Long userId, Long savedId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_NOT_FOUND));

        UserSave userSave = userSaveRepository.findByIdAndUser(savedId, user)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.SAVED_POST_NOT_FOUND));

        if (!Boolean.TRUE.equals(userSave.getIsSaved())) {
            throw new ProjectException(GeneralErrorCode.ALREADY_UNSAVED_POST);
        }

        userSave.cancelSave();

        return SavedPostConverter.toDeleteSavedPostResponse(userSave);
    }
}
