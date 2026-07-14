package umc.fitme.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecommendationRefreshService {

    public void refresh(Long userId) {
        // TODO: 추천 알고리즘 재계산 연동 (모듈 구현 후 교체)
        log.info("[Recommendation] TODO: recompute recommendations for userId={}", userId);
    }
}