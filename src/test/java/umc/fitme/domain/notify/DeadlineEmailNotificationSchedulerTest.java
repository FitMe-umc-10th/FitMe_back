package umc.fitme.domain.notify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import umc.fitme.domain.notify.service.DeadlineEmailNotificationService;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.global.scheduler.BaseScheduler;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeadlineEmailNotificationSchedulerTest {

    @Test
    @DisplayName("스케줄러 실행 시 Asia/Seoul 기준 오늘 날짜로 마감일 이메일 알림 서비스를 호출한다")
    void sendDeadlineReminderEmails_callServiceWithToday() {
        DeadlineEmailNotificationService service = mock(DeadlineEmailNotificationService.class);
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-07-18T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        BaseScheduler scheduler =
                new BaseScheduler(mock(PostRepository.class), service, fixedClock);

        scheduler.sendDeadlineReminderEmails();

        verify(service).sendDeadlineReminderEmails(LocalDate.of(2026, 7, 18));
    }

    @Test
    @DisplayName("local 프로필에서는 마감일 이메일 스케줄러 Bean을 등록하지 않는다")
    void deadlineEmailNotificationSchedulerBean_notRegisteredOnLocalProfile() {
        try (AnnotationConfigApplicationContext context = createContext("local")) {
            assertThrows(
                    NoSuchBeanDefinitionException.class,
                    () -> context.getBean(BaseScheduler.class)
            );
        }
    }

    @Test
    @DisplayName("ec2 프로필에서는 마감일 이메일 스케줄러 Bean을 등록한다")
    void deadlineEmailNotificationSchedulerBean_registeredOnEc2Profile() {
        try (AnnotationConfigApplicationContext context = createContext("ec2")) {
            assertNotNull(context.getBean(BaseScheduler.class));
        }
    }

    private AnnotationConfigApplicationContext createContext(String activeProfile) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles(activeProfile);
        context.register(BaseScheduler.class, SchedulerTestConfig.class);
        context.refresh();
        return context;
    }

    @Configuration
    static class SchedulerTestConfig {

        @Bean
        PostRepository postRepository() {
            return mock(PostRepository.class);
        }

        @Bean
        DeadlineEmailNotificationService deadlineEmailNotificationService() {
            return mock(DeadlineEmailNotificationService.class);
        }

        @Bean
        Clock clock() {
            return Clock.system(ZoneId.of("Asia/Seoul"));
        }
    }
}
