package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.user.dto.MyPageResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.enums.Status;
import umc.fitme.domain.user.repository.UserApplicationRepository;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserApplicationRepository userApplicationRepository;

    /**
     * 마이페이지에 필요한 사용자 정보, 지원 현황, 장학금 합격 금액을 조회합니다.
     *
     * @param userId 조회 대상(로그인한) 사용자 식별자
     * @return 마이페이지 응답 정보
     */
    public MyPageResponseDto.MyPageResponse getMyPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        UserDetail userDetail = userDetailRepository.findByUser(user)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_DETAIL_NOT_FOUND));

        long completedApplicationCount = userApplicationRepository.countByUserAndStatusIn(
                user,
                List.of(Status.PENDING_RESULT, Status.DOCUMENT_PASSED, Status.FINAL_PASSED)
        );

        long pendingResultCount = userApplicationRepository.countByUserAndStatus(
                user, Status.PENDING_RESULT
        );

        long totalScholarshipAmount = userApplicationRepository.sumFinalPassedScholarshipAmount(
                user, Status.FINAL_PASSED
        );

        return MyPageResponseDto.MyPageResponse.of(
                user,
                userDetail,
                completedApplicationCount,
                totalScholarshipAmount,
                pendingResultCount
        );
    }
}
