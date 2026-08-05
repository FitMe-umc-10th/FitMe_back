package umc.fitme.global.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.fitme.domain.post.service.PostExpirationService;

@Component
@RequiredArgsConstructor
@Profile("ec2") // EC2 환경에서만 실행
public class PostExpirationScheduler {

    private final PostExpirationService postExpirationService;

    /***
     * 스케줄러 기능: 매일 새벽 1시에 마감일이 지난 공고(장학금/공모전 등 타입 무관)를 비활성화한다.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void deactivateExpiredPosts() {
        postExpirationService.deactivateExpiredPosts();
    }
}
