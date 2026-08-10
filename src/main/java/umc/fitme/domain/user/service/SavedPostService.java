package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.post.exception.code.PostErrorCode;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.user.converter.SavedPostConverter;
import umc.fitme.domain.user.dto.SavedPostResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserSave;
import umc.fitme.domain.user.enums.SavedPostCategory;
import umc.fitme.domain.user.enums.SavedPostSort;
import umc.fitme.domain.user.exception.UserException;
import umc.fitme.domain.user.exception.code.SavedPostErrorCode;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.domain.user.repository.UserSaveRepository;
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
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

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
                items,
                nextCursor,
                size,
                hasNext
        );
    }

    /**
     * 함수 기능: 공고를 찜한다.
     * @param userId 유저 ID
     * @param postId 공고 ID
     * @return SavePostResponse Dto
     */
    @Transactional
    public SavedPostResponseDto.SavePostResponse savePost(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ProjectException(PostErrorCode.POST_NOT_FOUND));

        // 기존 행이 있으면 잠근 채 읽으므로, 경합한 요청은 앞선 요청이 커밋한 isSaved=true를 보고
        // resave()에서 409로 걸러진다. 여기까지 온 요청만 찜 수를 올릴 자격이 있다.
        UserSave saved = userSaveRepository.findByUserAndPost(user, post)
                .map(this::resave) // 이미 찜한 이력이 있다면
                .orElseGet(() -> createSave(user, post)); // 새로 찜한 경우 -> 새로 생성

        try {
            saved = userSaveRepository.saveAndFlush(saved);
        } catch (DataIntegrityViolationException e) {
            // findByUserAndPost 조회와 saveAndFlush 사이의 경합으로 동시에 두 요청이
            // 둘 다 "미저장"으로 판단해 INSERT를 시도할 수 있다. (user_id, post_id) 유니크 제약이
            // 이를 막아주고, 진 쪽은 여기서 ALREADY_SAVED_POST(409)로 변환되어 500을 피한다.
            throw new ProjectException(SavedPostErrorCode.ALREADY_SAVED_POST);
        } catch (ConcurrencyFailureException e) {
            // 행이 없을 때의 락킹 읽기는 유니크 인덱스에 갭 락을 잡는다. 두 트랜잭션이 같은 갭을
            // 잡은 뒤 서로의 INSERT를 기다리면 데드락(또는 락 대기 timeout)이 된다.
            // 이 경로에서의 경합 상대는 같은 (user, post)를 저장하려는 요청뿐이므로,
            // 유니크 제약 위반과 동일하게 409로 변환한다. 진 트랜잭션은 통째로 롤백되어
            // 찜 수가 중복 반영될 여지는 없다.
            throw new ProjectException(SavedPostErrorCode.ALREADY_SAVED_POST);
        }

        // 상태 전이에 성공한 요청만 도달하므로, 전이 1회당 카운트 1회가 보장된다.
        postRepository.increaseSavedCount(postId);

        return SavedPostConverter.toSavePostResponse(saved);
    }

    // 저장 이력을 새로 생성한다. (찜 수 증가는 호출부에서 원자적 UPDATE로 처리한다)
    private UserSave createSave(User user, Post post) {
        return UserSave.builder()
                        .user(user)
                        .post(post)
                        .isSaved(true)
                        .build();
    }

    // 해당 공고 저장 이력이 존재한다면 isSaved = false -> true로 바꾼다.
    private UserSave resave(UserSave userSave) {
        // isSaved가 true라면 예외를 반환한다.
        if (Boolean.TRUE.equals(userSave.getIsSaved())){
            throw new ProjectException(SavedPostErrorCode.ALREADY_SAVED_POST);
        }
        userSave.resave();
        return userSave;
    }

    private void validateSize(Integer size) {
        if (size == null || size <= 0 || size > MAX_SIZE) {
            throw new ProjectException(SavedPostErrorCode.INVALID_PAGE_SIZE);
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

        PostType postType = convertCategory(category);
        if (savedIdCursor == null) {
            return userSaveRepository.findAllByUserAndIsSavedTrueAndPost_PostTypeOrderByIdDesc(
                    user, postType, pageable
            );
        }
        return userSaveRepository.findAllByUserAndIsSavedTrueAndPost_PostTypeAndIdLessThanOrderByIdDesc(
                user, postType, savedIdCursor, pageable
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
            throw new ProjectException(SavedPostErrorCode.INVALID_CURSOR);
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
            throw new ProjectException(SavedPostErrorCode.INVALID_CURSOR);
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
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 잠근 채 읽으므로 동시에 들어온 취소 요청 중 하나만 isSaved=true를 보고 통과한다.
        // 나머지는 조회 자체가 비어 SAVED_POST_NOT_FOUND가 되어 카운트를 중복 차감하지 않는다.
        UserSave userSave = userSaveRepository.findByIdAndUserAndIsSavedTrue(savedId, user)
                .orElseThrow(() -> new ProjectException(SavedPostErrorCode.SAVED_POST_NOT_FOUND));

        userSave.cancelSave();

        // 프록시에서 식별자만 꺼내므로 Post를 초기화하지 않는다.
        postRepository.decreaseSavedCount(userSave.getPost().getId());

        return SavedPostConverter.toDeleteSavedPostResponse(userSave);
    }
}
