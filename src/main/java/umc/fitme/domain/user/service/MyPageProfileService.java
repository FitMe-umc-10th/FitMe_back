  package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.interest.entity.Interest;
import umc.fitme.domain.interest.entity.mapping.UserInterest;
import umc.fitme.domain.interest.repository.InterestRepository;
import umc.fitme.domain.interest.repository.UserInterestRepository;
import umc.fitme.domain.user.dto.MyPageProfileRequestDto;
import umc.fitme.domain.user.dto.MyPageProfileResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageProfileService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final RecommendationRefreshService recommendationRefreshService;

    /**
     * 마이페이지 프로필을 조회합니다.
     *
     * @param userId 조회 대상(로그인한) 사용자 식별자
     * @return 프로필 정보(관심 분야는 전체 목록 + 선택 여부)
     */
    public MyPageProfileResponseDto.ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        UserDetail detail = userDetailRepository.findByUser(user)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_DETAIL_NOT_FOUND));

        return MyPageProfileResponseDto.ProfileResponse.of(user, detail, buildInterestItems(user));
    }

    /**
     * 마이페이지 프로필을 부분 수정합니다. (값이 오는 필드만 병합)
     *
     * @param userId 수정 대상(로그인한) 사용자 식별자
     * @param request 부분 수정 요청(모든 필드 nullable)
     * @return 수정된 프로필 정보
     */
    @Transactional
    public MyPageProfileResponseDto.UpdateProfileResponse updateProfile(
            Long userId, MyPageProfileRequestDto.UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        UserDetail detail = userDetailRepository.findByUser(user)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_DETAIL_NOT_FOUND));

        // 부분수정인데 아무 필드도 오지 않으면 수정할 항목이 없음
        if (request.gpa() == null
                && request.incomeBracket() == null
                && request.region() == null
                && request.interests() == null
                && request.profileImageUrl() == null) {
            throw new ProjectException(UserErrorCode.PROFILE_UPDATE_EMPTY);
        }

        // 스칼라 필드 병합 (null 이면 기존값 유지)
        Float newGpa = request.gpa() != null ? request.gpa().floatValue() : detail.getGpa();
        int newIncome = request.incomeBracket() != null ? request.incomeBracket() : detail.getIncomeBracket();
        String newRegion = request.region() != null ? request.region() : detail.getRegion();
        detail.updateProfile(newGpa, newIncome, newRegion);

        if (request.profileImageUrl() != null) {
            detail.updateProfileImage(request.profileImageUrl());
        }

        // 관심분야 변경 요청이 있으면 전체 교체
        if (request.interests() != null) {
            List<Long> ids = request.interests().stream()
                    .distinct()
                    .collect(Collectors.toList());

            List<Interest> found = interestRepository.findAllById(ids);
            if (found.size() != ids.size()) {
                throw new ProjectException(UserErrorCode.INTEREST_NOT_FOUND);
            }

            userInterestRepository.deleteAllByUser(user);
            userInterestRepository.flush();
            List<UserInterest> userInterests = found.stream()
                    .map(it -> UserInterest.builder()
                            .user(user)
                            .interest(it)
                            .build())
                    .collect(Collectors.toList());
            userInterestRepository.saveAll(userInterests);
        }

        recommendationRefreshService.refresh(userId);

        return MyPageProfileResponseDto.UpdateProfileResponse.of(detail, buildInterestItems(user));
    }

    /**
     * 관심 분야 전체 목록에 사용자의 선택 여부(selected)를 표시해 반환합니다.
     * getProfile / updateProfile 양쪽에서 응답 조립에 재사용합니다.
     */
    private List<MyPageProfileResponseDto.InterestItem> buildInterestItems(User user) {
        Set<Long> selectedIds = userInterestRepository.findAllByUser(user).stream()
                .map(userInterest -> userInterest.getInterest().getId())
                .collect(Collectors.toSet());

        return interestRepository.findAllByOrderByIdAsc().stream()
                .map(interest -> MyPageProfileResponseDto.InterestItem.of(
                        interest, selectedIds.contains(interest.getId())))
                .collect(Collectors.toList());
    }
}