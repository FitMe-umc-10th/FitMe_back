package umc.fitme.domain.user.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class MyPageProfileRequestDto {

    /* PATCH 프로필 부분수정 요청 (모든 필드 nullable) */
    public record UpdateProfileRequest(
            @DecimalMin(value = "0.00", message = "유효하지 않은 학점입니다.")
            @DecimalMax(value = "4.50", message = "유효하지 않은 학점입니다.")
            @Digits(integer = 1, fraction = 2, message = "유효하지 않은 학점입니다.")
            BigDecimal gpa,

            @Min(value = 1, message = "유효하지 않은 소득구간입니다.")
            @Max(value = 10, message = "유효하지 않은 소득구간입니다.")
            Integer incomeBracket,

            String region,

            @Size(min = 1, message = "관심 분야는 최소 1개 이상 선택해야 합니다.")
            List<@NotNull(message = "관심 분야 ID에 null 은 허용되지 않습니다.") Long> interests,

            String profileImageUrl
    ) {
    }
}