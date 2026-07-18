package umc.fitme.domain.onboarding.dto;

public record OnboardingResponseDto(
        Boolean onboardingCompleted
) {
    public static OnboardingResponseDto of(boolean onboardingCompleted) {
        return new OnboardingResponseDto(onboardingCompleted);
    }
}
