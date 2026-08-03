package umc.fitme.global.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.fitme.domain.post.service.PostSummaryService;

@Component
@RequiredArgsConstructor
@Profile("ec2") // EC2 환경에서만 실행
public class PostSummaryScheduler {

    private final PostSummaryService postSummaryService;

    /***
     * 스케줄러 기능: 10분마다 AI 요약이 캐싱되지 않은 활성 공고(장학금/공모전 등 타입 무관)를 찾아 생성한다.
     */
    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
    public void generateMissingSummaries() {
        postSummaryService.generateMissingSummaries();
    }
}
