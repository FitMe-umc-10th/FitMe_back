package umc.fitme.domain.onboarding.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.interest.entity.Interest;
import umc.fitme.domain.interest.entity.mapping.UserInterest;
import umc.fitme.domain.interest.repository.InterestRepository;
import umc.fitme.domain.interest.repository.UserInterestRepository;
import umc.fitme.domain.onboarding.dto.OnboardingRequestDto;
import umc.fitme.domain.onboarding.dto.OnboardingResponseDto;
import umc.fitme.domain.onboarding.exception.OnboardingException;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetailRepository userDetailRepository;
    @Mock
    private InterestRepository interestRepository;
    @Mock
    private UserInterestRepository userInterestRepository;

    @InjectMocks
    private OnboardingService onboardingService;

    @Test
    @DisplayName("온보딩 완료 성공")
    void complete_success() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .isOnboarded(false)
                .build();

        OnboardingRequestDto request = new OnboardingRequestDto(
                "서울",
                "홍익대학교",
                4.1f,
                "3구간",
                List.of("개발", "AI"),
                List.of("백엔드", "AI")
        );

        Interest develop = Interest.builder().interestName("개발").build();
        Interest ai = Interest.builder().interestName("AI").build();
        Interest backend = Interest.builder().interestName("백엔드").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(interestRepository.findByInterestNameIn(anyList())).willReturn(List.of(develop, ai));
        given(interestRepository.saveAll(anyList())).willReturn(List.of(backend));

        OnboardingResponseDto response = onboardingService.complete(userId, request);

        assertThat(response.onboardingCompleted()).isTrue();

        ArgumentCaptor<UserDetail> userDetailCaptor = ArgumentCaptor.forClass(UserDetail.class);
        verify(userDetailRepository).save(userDetailCaptor.capture());
        UserDetail savedUserDetail = userDetailCaptor.getValue();

        assertThat(savedUserDetail.getRegion()).isEqualTo("서울");
        assertThat(savedUserDetail.getUniversityName()).isEqualTo("홍익대학교");
        assertThat(savedUserDetail.getGpa()).isEqualTo(4.1f);
        assertThat(savedUserDetail.getIncomeBracket()).isEqualTo(3);

        ArgumentCaptor<List<UserInterest>> userInterestCaptor = ArgumentCaptor.forClass(List.class);
        verify(userInterestRepository).saveAll(userInterestCaptor.capture());
        assertThat(userInterestCaptor.getValue()).hasSize(3);

        verify(interestRepository).findByInterestNameIn(anyList());
        verify(interestRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("이미 온보딩 완료된 사용자는 예외 발생")
    void complete_fail_alreadyOnboarded() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .isOnboarded(true)
                .build();

        OnboardingRequestDto request = new OnboardingRequestDto(
                "서울",
                "홍익대학교",
                4.1f,
                "3구간",
                List.of("개발"),
                List.of("백엔드")
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> onboardingService.complete(userId, request))
                .isInstanceOf(OnboardingException.class);

        verify(userDetailRepository, never()).save(any());
        verify(userInterestRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("존재하지 않는 유저는 예외 발생")
    void complete_fail_userNotFound() {
        Long userId = 1L;
        OnboardingRequestDto request = new OnboardingRequestDto(
                "서울",
                "홍익대학교",
                4.1f,
                "3구간",
                List.of("개발"),
                List.of("백엔드")
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> onboardingService.complete(userId, request))
                .isInstanceOf(ProjectException.class);
    }

    @Test
    @DisplayName("필수값 누락 시 예외 발생")
    void complete_fail_missingRequiredField() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .isOnboarded(false)
                .build();

        OnboardingRequestDto request = new OnboardingRequestDto(
                null,
                "홍익대학교",
                4.1f,
                "3구간",
                List.of("개발"),
                List.of("백엔드")
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> onboardingService.complete(userId, request))
                .isInstanceOf(OnboardingException.class);
    }

    @Test
    @DisplayName("학점 범위가 유효하지 않으면 예외 발생")
    void complete_fail_invalidGpaRange() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .isOnboarded(false)
                .build();

        OnboardingRequestDto request = new OnboardingRequestDto(
                "서울",
                "홍익대학교",
                4.8f,
                "3구간",
                List.of("개발"),
                List.of("백엔드")
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> onboardingService.complete(userId, request))
                .isInstanceOf(OnboardingException.class);
    }

    @Test
    @DisplayName("관심분야가 모두 비어 있으면 예외 발생")
    void complete_fail_interestRequired() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .isOnboarded(false)
                .build();

        OnboardingRequestDto request = new OnboardingRequestDto(
                "서울",
                "홍익대학교",
                4.1f,
                "3구간",
                List.of(),
                List.of()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> onboardingService.complete(userId, request))
                .isInstanceOf(OnboardingException.class);
    }
}