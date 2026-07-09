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
import umc.fitme.domain.user.repository.UserApplicationRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApplicationService {

    private static final Long TEMP_USER_ID = 1L;

    private final UserApplicationRepository userApplicationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public UserApplicationResponseDto.CreateResponse create(
            UserApplicationRequestDto.CreateRequest request
    ) {
        User user = userRepository.findById(TEMP_USER_ID)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.POST_NOT_FOUND));

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

    public UserApplicationResponseDto.ListResponse getList(String tab) {
        User user = userRepository.findById(TEMP_USER_ID)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_NOT_FOUND));

        List<Status> statuses = switch (tab) {
            case "IN_PROGRESS" -> List.of(
                    Status.NONE,
                    Status.PENDING_RESULT,
                    Status.DOCUMENT_PASSED
            );
            case "FINAL_PASSED" -> List.of(Status.FINAL_PASSED);
            default -> throw new ProjectException(GeneralErrorCode.INVALID_USER_APPLICATION_TAB);
        };

        List<UserApplication> userApplications =
                userApplicationRepository.findAllByUserAndStatusInOrderByUpdatedAtDescIdDesc(
                        user, statuses
                );

        return UserApplicationResponseDto.ListResponse.from(userApplications);
    }

    public UserApplicationResponseDto.DetailResponse getDetail(Long userApplicationId) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }

    @Transactional
    public UserApplicationResponseDto.UpdateStatusResponse updateStatus(
            Long userApplicationId,
            UserApplicationRequestDto.UpdateStatusRequest request
    ) {
        User user = userRepository.findById(TEMP_USER_ID)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_NOT_FOUND));

        UserApplication userApplication =
                userApplicationRepository.findByIdAndUser(
                        userApplicationId, user
                ).orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_APPLICATION_NOT_FOUND));

        if (request.status() == null || request.status() == Status.NONE) {
            throw new ProjectException(GeneralErrorCode.INVALID_USER_APPLICATION_STATUS);
        }

        userApplication.updateStatus(request.status());

        return UserApplicationResponseDto.UpdateStatusResponse.from(userApplication);
    }

    @Transactional
    public UserApplicationResponseDto.UpdateMemoResponse updateMemo(
            Long userApplicationId,
            UserApplicationRequestDto.UpdateMemoRequest request
    ) {
        User user = userRepository.findById(TEMP_USER_ID)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_NOT_FOUND));

        UserApplication userApplication = userApplicationRepository.findByIdAndUser(userApplicationId, user)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_APPLICATION_NOT_FOUND));

        String memo = request.memo();
        String trimmedMemo = memo == null ? null : memo.trim();

        if (trimmedMemo != null && trimmedMemo.length() > 1000) {
            throw new ProjectException(GeneralErrorCode.MEMO_TOO_LONG);
        }

        userApplication.updateMemo(trimmedMemo);

        return UserApplicationResponseDto.UpdateMemoResponse.from(userApplication);
    }

    @Transactional
    public UserApplicationResponseDto.DeleteResponse delete(Long userApplicationId) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }
}