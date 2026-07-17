package umc.fitme.domain.user.converter;

import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.PostType;
import umc.fitme.domain.user.dto.SavedPostResponseDto;
import umc.fitme.domain.user.entity.mapping.UserSave;
import umc.fitme.domain.user.enums.SavedPostCategory;
import umc.fitme.domain.user.exception.code.SavedPostErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SavedPostConverter {

    public static SavedPostResponseDto.SavedPostListResponse toSavedPostListResponse(
            List<SavedPostResponseDto.SavedPostItem> savedPosts,
            String nextCursor,
            Integer size,
            Boolean hasNext
    ) {
        return SavedPostResponseDto.SavedPostListResponse.builder()
                .savedPosts(savedPosts)
                .pageInfo(toPageInfo(nextCursor, size, hasNext))
                .build();
    }

    public static SavedPostResponseDto.SavedPostItem toSavedPostItem(UserSave userSave) {
        Post post = userSave.getPost();

        return SavedPostResponseDto.SavedPostItem.builder()
                .savedId(userSave.getId())
                .postId(post.getId())
                .type(post.getPostType().name())
                .title(post.getTitle())
                .organization(post.getOrganizer())
                .thumbnailUrl(post.getImageUrl())
                .saved(userSave.getIsSaved())
                .deadlineLabel(toDeadlineLabel(post.getApplyEndAt()))
                .deadlineDate(post.getApplyEndAt())
                .savedAt(userSave.getCreatedAt())
                .build();
    }

    public static SavedPostResponseDto.SavePostResponse toSavePostResponse(UserSave userSave) {
        return SavedPostResponseDto.SavePostResponse.builder()
                .savedId(userSave.getId())
                .postId(userSave.getPost().getId())
                .saved(userSave.getIsSaved())
                .build();
    }

    public static List<SavedPostResponseDto.SavedPostItem> toSavedPostItemList(List<UserSave> userSaves) {
        return userSaves.stream()
                .map(SavedPostConverter::toSavedPostItem)
                .toList();
    }

    public static SavedPostResponseDto.PageInfo toPageInfo(
            String nextCursor,
            Integer size,
            Boolean hasNext
    ) {
        return SavedPostResponseDto.PageInfo.builder()
                .nextCursor(nextCursor)
                .size(size)
                .hasNext(hasNext)
                .build();
    }

    private static String toDeadlineLabel(LocalDate deadlineDate) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), deadlineDate);

        if (dDay < 0) {
            return "마감";
        }

        if (dDay == 0) {
            return "D-Day";
        }

        return "D-" + dDay;
    }

    public static PostType convertCategory(SavedPostCategory category) {
        return switch (category) {
            case SCHOLARSHIP -> PostType.SCHOLARSHIP;
            case CONTEST -> PostType.CONTEST;
            default -> throw new ProjectException(SavedPostErrorCode.INVALID_CATEGORY);
        };
    }

    public static SavedPostResponseDto.DeleteSavedPostResponse toDeleteSavedPostResponse(UserSave userSave) {
        return SavedPostResponseDto.DeleteSavedPostResponse.builder()
                .savedId(userSave.getId())
                .postId(userSave.getPost().getId())
                .saved(userSave.getIsSaved())
                .build();
    }
}
