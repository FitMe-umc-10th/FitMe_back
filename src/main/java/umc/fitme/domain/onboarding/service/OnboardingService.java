package umc.fitme.domain.onboarding.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.interest.entity.Interest;
import umc.fitme.domain.interest.entity.mapping.UserInterest;
import umc.fitme.domain.interest.repository.InterestRepository;
import umc.fitme.domain.interest.repository.UserInterestRepository;
import umc.fitme.domain.onboarding.dto.OnboardingRequestDto;
import umc.fitme.domain.onboarding.dto.OnboardingResponseDto;
import umc.fitme.domain.onboarding.exception.OnboardingException;
import umc.fitme.domain.onboarding.exception.code.OnboardingErrorCode;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

    private static final Pattern INCOME_LEVEL_DIGIT_PATTERN = Pattern.compile("\\d+");
    private static final float MIN_GPA = 0.0f;
    private static final float MAX_GPA = 4.5f;

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;

    @Transactional
    public OnboardingResponseDto complete(Long userId, OnboardingRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsOnboarded())) {
            throw new OnboardingException(OnboardingErrorCode.ALREADY_ONBOARDED);
        }

        validate(request);

        UserDetail userDetail = UserDetail.builder()
                .user(user)
                .region(request.region())
                .universityName(request.university())
                .gpa(request.gpa())
                .incomeBracket(parseIncomeLevel(request.incomeLevel()))
                .build();
        userDetailRepository.save(userDetail);

        List<String> interestNames = mergeInterestNames(request);
        List<UserInterest> userInterests = interestNames.stream()
                .map(name -> UserInterest.builder()
                        .user(user)
                        .interest(getOrCreateInterest(name))
                        .build())
                .toList();
        userInterestRepository.saveAll(userInterests);

        user.completeOnboarding();

        return OnboardingResponseDto.of(true);
    }

    private void validate(OnboardingRequestDto request) {
        if (isBlank(request.region())
                || isBlank(request.university())
                || request.gpa() == null
                || isBlank(request.incomeLevel())) {
            throw new OnboardingException(OnboardingErrorCode.MISSING_REQUIRED_FIELD);
        }

        if (request.gpa() < MIN_GPA || request.gpa() > MAX_GPA) {
            throw new OnboardingException(OnboardingErrorCode.INVALID_GPA_RANGE);
        }

        boolean hasInterests = request.interests() != null && !request.interests().isEmpty();
        boolean hasCustomInterests = request.customInterests() != null && !request.customInterests().isEmpty();
        if (!hasInterests && !hasCustomInterests) {
            throw new OnboardingException(OnboardingErrorCode.INTEREST_REQUIRED);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int parseIncomeLevel(String incomeLevel) {
        Matcher matcher = INCOME_LEVEL_DIGIT_PATTERN.matcher(incomeLevel);
        if (!matcher.find()) {
            throw new OnboardingException(OnboardingErrorCode.MISSING_REQUIRED_FIELD);
        }
        return Integer.parseInt(matcher.group());
    }

    private List<String> mergeInterestNames(OnboardingRequestDto request) {
        Stream<String> tagStream = request.interests() == null ? Stream.empty() : request.interests().stream();
        Stream<String> customStream = request.customInterests() == null ? Stream.empty() : request.customInterests().stream();

        return Stream.concat(tagStream, customStream)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private Interest getOrCreateInterest(String interestName) {
        return interestRepository.findByInterestName(interestName)
                .orElseGet(() -> interestRepository.save(
                        Interest.builder().interestName(interestName).build()
                ));
    }
}
