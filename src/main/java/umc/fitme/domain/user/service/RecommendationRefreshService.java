package umc.fitme.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecommendationRefreshService {

    public void refresh(Long userId) {
        // 홈 추천(FIT)은 홈 탭 조회 시점에 사용자 프로필을 실시간으로 읽어 계산하므로,
        // 프로필 저장이 커밋되면 다음 홈 조회에서 자동 반영된다 → 별도 재계산/재동기화 불필요.
        log.debug("[Recommendation] profile updated for userId={} (recomputed live on home fetch)", userId);
    }
}