  package umc.fitme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

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

        // 값이 온 필드만 개별 반영 (안 온 필드는 UPDATE 문에도 실리지 않음 - @DynamicUpdate)
        if (request.gpa() != null) {
            detail.updateGpa(request.gpa().floatValue());
        }
        if (request.incomeBracket() != null) {
            detail.updateIncomeBracket(request.incomeBracket());
        }
        if (request.region() != null) {
            detail.updateRegion(request.region());
        }

        if (request.profileImageUrl() != null) {
            validateOwnedProfileImageUrl(userId, request.profileImageUrl());
            detail.updateProfileImage(request.profileImageUrl());
        }

        // 관심분야 변경 요청이 있으면 차집합만 반영 (전량 삭제-재삽입 대신 diff)
        if (request.interests() != null) {
            replaceInterests(user, request.interests());
        }

        recommendationRefreshService.refresh(userId);

        return MyPageProfileResponseDto.UpdateProfileResponse.of(detail, buildInterestItems(user));
    }

    /**
     * 프로필 이미지 URL이 본인 소유 경로(presigned URL 발급 시 사용한 key 프리픽스)인지 검증합니다.
     * 다른 사용자 경로나 외부 도메인 URL이 그대로 저장되는 것을 막습니다.
     *
     * @param userId 로그인한 사용자 식별자
     * @param profileImageUrl 요청으로 온 프로필 이미지 URL
     */
    private void validateOwnedProfileImageUrl(Long userId, String profileImageUrl) {
        String expectedPrefix = String.format("https://%s.s3.%s.amazonaws.com/%s%d/",
                bucket, region, ProfileImageService.KEY_PREFIX, userId);

        if (!profileImageUrl.startsWith(expectedPrefix)) {
            throw new ProjectException(UserErrorCode.INVALID_IMAGE_URL);
        }
    }

    /**
     * 사용자의 관심 분야를 요청 목록과 일치하도록 차집합(diff)만 반영합니다.
     *
     * @param user 대상 사용자
     * @param requestedIds 요청으로 온 관심 분야 식별자 목록
     */
    private void replaceInterests(User user, List<Long> requestedIds) {
        List<Long> ids = requestedIds.stream()
                .distinct()
                .collect(Collectors.toList());

        List<Interest> found = interestRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new ProjectException(UserErrorCode.INTEREST_NOT_FOUND);
        }

        Set<Long> targetIds = found.stream()
                .map(Interest::getId)
                .collect(Collectors.toSet());

        List<UserInterest> currents = userInterestRepository.findAllByUser(user);
        Set<Long> currentIds = currents.stream()
                .map(userInterest -> userInterest.getInterest().getId())
                .collect(Collectors.toSet());

        // current - target : 요청에서 빠진 것만 삭제 (영속 엔티티를 그대로 넘겨 컨텍스트와 어긋나지 않게)
        List<UserInterest> toDelete = currents.stream()
                .filter(userInterest -> !targetIds.contains(userInterest.getInterest().getId()))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            userInterestRepository.deleteAll(toDelete);
        }

        // target - current : 새로 선택된 것만 추가 (교집합은 그대로 유지)
        List<UserInterest> toInsert = found.stream()
                .filter(interest -> !currentIds.contains(interest.getId()))
                .map(interest -> UserInterest.builder()
                        .user(user)
                        .interest(interest)
                        .build())
                .collect(Collectors.toList());
        if (!toInsert.isEmpty()) {
            userInterestRepository.saveAll(toInsert);
        }
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