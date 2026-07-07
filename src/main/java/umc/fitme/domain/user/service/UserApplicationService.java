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
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

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
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Status> statuses = switch (tab) {
            case "IN_PROGRESS" -> List.of(
                    Status.NONE,
                    Status.PENDING_RESULT,
                    Status.DOCUMENT_PASSED
            );
            case "FINAL_PASSED" -> List.of(Status.FINAL_PASSED);
            default -> throw new IllegalArgumentException("유효하지 않은 이력 탭입니다.");
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
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }

    @Transactional
    public UserApplicationResponseDto.UpdateMemoResponse updateMemo(
            Long userApplicationId,
            UserApplicationRequestDto.UpdateMemoRequest request
    ) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }

    @Transactional
    public UserApplicationResponseDto.DeleteResponse delete(Long userApplicationId) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }
}