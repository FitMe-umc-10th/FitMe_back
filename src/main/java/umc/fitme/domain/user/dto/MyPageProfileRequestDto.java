package umc.fitme.domain.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class MyPageProfileRequestDto {

    /* PATCH 프로필 부분수정 요청 (모든 필드 nullable) */
    public record UpdateProfileRequest(
            @DecimalMin("0.00") @DecimalMax("4.50") @Digits(integer = 1, fraction = 2)
            BigDecimal gpa,

            @Min(1) @Max(10)
            Integer incomeBracket,

            String region,

            @Size(min = 1, message = "관심 분야는 최소 1개 이상 선택해야 합니다.")
            List<@NotNull(message = "관심 분야 ID에 null 은 허용되지 않습니다.") Long> interests,

            String profileImageUrl
    ) {
    }
}