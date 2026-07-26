package umc.fitme.domain.post.sync.dto;

public record ScholarshipCsvRow(
        String organization,      // 운영기관
        String productName,       // 상품명
        String productType,       // 상품구분
        String supportType,       // 학자금 지원 유형
        String applicantTarget,   // 신청대상
        String applyPeriodRaw,    // 신청기간 (원본 문자열, 예: "2026-03-01 ~ 2026-03-31")
        String supportAmount,     // 지원금액
        String supportCount       // 지원인원
) {
}
