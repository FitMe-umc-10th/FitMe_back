package umc.fitme.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.notify.service.DeadlineEmailNotificationService;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.repository.PostRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("ec2") // EC2 환경에서만 실행
public class BaseScheduler {

    private final PostRepository postRepository;
    private final DeadlineEmailNotificationService deadlineEmailNotificationService;
    private final Clock clock;

    /***
     * 스케줄러 기능: 매일 0시 0분 0초에 조회수 기반으로 공고 순위를 계산하여 업데이트한다.
     *              공고는 조회수 기준 상위 8개에 대해서만이다.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void updateRank(){

        // 기존 순위권 공고들의 rank를 -1로 모두 리셋
        // 이유: 전날 5위였던 공고가 순위권을 탈출하고 다시 3위가 되면 NEW가 아닌 UP이 되기 때문에
        postRepository.resetAllRanks();

        // 조회수 상위 8개 공고 리스트
        List<Post> top8ByViewCount = postRepository.findTop8ByViewCount();

        // 순위 갱신
        for (int i = 0; i < top8ByViewCount.size(); i++){
            Post post = top8ByViewCount.get(i);
            int rank = i+1;
            post.updateRank(rank);
        }
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendDeadlineReminderEmails() {
        LocalDate today = LocalDate.now(clock);

        // 잡 경계에서 실패를 잡아 맥락과 함께 남긴다.
        // 스프링 기본 처리에 맡기면 "Unexpected error occurred in scheduled task"만 남아
        // 어떤 배치가 어떤 날짜에 실패했는지 알 수 없다.
        try {
            deadlineEmailNotificationService.sendDeadlineReminderEmails(today);
        } catch (Exception e) {
            log.error("마감 임박 메일 발송 배치 실패 - date={}", today, e);
        }
    }
}
