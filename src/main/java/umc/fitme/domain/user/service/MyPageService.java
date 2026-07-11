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
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private static final Long TEMP_USER_ID = 1L;

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserApplicationRepository userApplicationRepository;

    /**
     * 마이페이지에 필요한 사용자 정보, 지원 현황, 장학금 합격 금액을 조회합니다.
     *
     * @return 마이페이지 응답 정보
     */
    public MyPageResponseDto.MyPageResponse getMyPage() {
        User user = userRepository.findById(TEMP_USER_ID)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_NOT_FOUND));

        UserDetail userDetail = userDetailRepository.findByUser(user)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.USER_NOT_FOUND));

        long completedApplicationCount = userApplicationRepository.countByUserAndStatusIn(
                user,
                List.of(Status.PENDING_RESULT, Status.DOCUMENT_PASSED, Status.FINAL_PASSED)
        );

        long pendingResultCount = userApplicationRepository.countByUserAndStatus(
                user, Status.PENDING_RESULT
        );

        List<String> amounts = userApplicationRepository.findFinalPassedScholarshipAmounts(
                user, Status.FINAL_PASSED
        );
        long totalScholarshipAmount = amounts.stream()
                .mapToLong(MyPageService::parseAmount)
                .sum();

        return MyPageResponseDto.MyPageResponse.of(
                user,
                userDetail,
                completedApplicationCount,
                totalScholarshipAmount,
                pendingResultCount
        );
    }

    /**
     * 문자열 형태의 장학금 금액을 long 타입으로 변환합니다.
     * "만"이 포함되면 "만" 앞의 숫자만 뽑아 파싱한 뒤 10,000을 곱하고,
     * 그렇지 않으면 숫자만 추출해 그대로 파싱합니다.
     *
     * @param amount 장학금 금액 문자열
     * @return 변환된 장학금 금액
     */
    static long parseAmount(String amount) {
        if (amount == null) {
            return 0L;
        }

        int manIndex = amount.indexOf('만');
        if (manIndex >= 0) {
            String digits = amount.substring(0, manIndex).replaceAll("[^0-9]", "");
            if (digits.isEmpty()) {
                return 0L;
            }
            try {
                return Long.parseLong(digits) * 10_000L;
            } catch (NumberFormatException e) {
                return 0L;
            }
        }

        String digits = amount.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0L;
        }

        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
