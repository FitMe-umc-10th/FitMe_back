package umc.fitme.domain.post.sync.dto;

public record ScholarshipSourceRow(
        String organization,      // 운영기관명
        String productName,       // 상품명
        String productType,       // 상품구분
        String supportType,       // 학자금유형구분
        String applicantTarget,   // 특정자격 상세내용
        String applyPeriodRaw,    // 모집시작일 ~ 모집종료일 조합 (예: "2026-03-01 ~ 2026-03-31")
        String supportAmount,     // 지원내역 상세내용
        String supportCount       // 선발인원 상세내용
) {
}
