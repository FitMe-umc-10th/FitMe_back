package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.user.dto.UserApplicationRequestDto;
import umc.fitme.domain.user.dto.UserApplicationResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.enums.Status;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserApplicationRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.exception.code.UserApplicationErrorCode;
import umc.fitme.domain.post.exception.code.PostErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApplicationService {

    private final UserApplicationRepository userApplicationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public UserApplicationResponseDto.CreateResponse create(
            Long userId,
            UserApplicationRequestDto.CreateRequest request
    ) {

        User user = getUser(userId);


        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new ProjectException(PostErrorCode.POST_NOT_FOUND));

        UserApplication userApplication = userApplicationRepository.findByUserAndPost(user, post)
                .orElseGet(() -> userApplicationRepository.save(
                        UserApplication.builder()
                                .user(user)
                                .post(post)
                                .status(Status.NONE)
                                .isApplied(false)
                                .memo(null)
                                .build()
                ));

        return UserApplicationResponseDto.CreateResponse.from(userApplication);
    }

    public UserApplicationResponseDto.ListResponse getList(Long userId, String tab) {
        User user = getUser(userId);


        List<Status> statuses = switch (tab) {
            case "IN_PROGRESS" -> List.of(
                    Status.NONE,
                    Status.PENDING_RESULT,
                    Status.DOCUMENT_PASSED
            );
            case "FINAL_PASSED" -> List.of(Status.FINAL_PASSED);
            default -> throw new ProjectException(UserApplicationErrorCode.INVALID_USER_APPLICATION_TAB);
        };

        List<UserApplication> userApplications =
                userApplicationRepository.findAllByUserAndStatusInOrderByUpdatedAtDescIdDesc(
                        user, statuses
                );

        return UserApplicationResponseDto.ListResponse.from(userApplications);
    }

    @Transactional
    public UserApplicationResponseDto.DetailResponse getDetail(Long userId, Long userApplicationId) {
        User user = getUser(userId);


        UserApplication userApplication = userApplicationRepository.findByIdAndUser(userApplicationId, user)
                .orElseThrow(() -> new ProjectException(UserApplicationErrorCode.USER_APPLICATION_NOT_FOUND));

        userApplication.getPost().increaseViewCount();

        return UserApplicationResponseDto.DetailResponse.from(userApplication);
    }

    @Transactional
    public UserApplicationResponseDto.UpdateStatusResponse updateStatus(
            Long userId,
            Long userApplicationId,
            UserApplicationRequestDto.UpdateStatusRequest request
    ) {
        User user = getUser(userId);

        UserApplication userApplication =
                userApplicationRepository.findByIdAndUser(userApplicationId, user)
                        .orElseThrow(() -> new ProjectException(UserApplicationErrorCode.USER_APPLICATION_NOT_FOUND));

        if (request.status() == null || request.status() == Status.NONE) {
            throw new ProjectException(UserApplicationErrorCode.INVALID_USER_APPLICATION_STATUS);
        }

        userApplication.updateStatus(request.status());

        return UserApplicationResponseDto.UpdateStatusResponse.from(userApplication);
    }

    @Transactional
    public UserApplicationResponseDto.UpdateMemoResponse updateMemo(
            Long userId,
            Long userApplicationId,
            UserApplicationRequestDto.UpdateMemoRequest request
    ) {

        User user = getUser(userId);

        UserApplication userApplication = userApplicationRepository.findByIdAndUser(userApplicationId, user)
                .orElseThrow(() -> new ProjectException(UserApplicationErrorCode.USER_APPLICATION_NOT_FOUND));

        // 정책 확정: API로 들어온 원본 memo 기준 1000자 검증, 그 후 trim 처리, trim 후 빈 문자열이면 null 저장
        String memo = request.memo();

        if (memo != null && memo.length() > 1000) {
            throw new ProjectException(UserApplicationErrorCode.MEMO_TOO_LONG);
        }

        String trimmedMemo = memo == null ? null : memo.trim();

        userApplication.updateMemo(trimmedMemo);

        return UserApplicationResponseDto.UpdateMemoResponse.from(userApplication);
    }

    @Transactional
    public UserApplicationResponseDto.DeleteResponse delete(Long userId, Long userApplicationId) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

    }
}