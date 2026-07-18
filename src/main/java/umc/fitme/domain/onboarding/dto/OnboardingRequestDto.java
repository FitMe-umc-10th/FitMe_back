package umc.fitme.domain.onboarding.dto;

import java.util.List;

public record OnboardingRequestDto(
        String region,
        String university,
        Float gpa,
        String incomeLevel,
        List<String> interests,
        List<String> customInterests
) {
}
