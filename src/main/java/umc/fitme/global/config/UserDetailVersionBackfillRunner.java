package umc.fitme.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserDetail 에 낙관적 락(@Version)을 도입하면서 추가된 version 컬럼을 백필합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailVersionBackfillRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int updated = jdbcTemplate.update("UPDATE user_detail SET version = 0 WHERE version IS NULL");
        if (updated > 0) {
            log.info("user_detail.version 백필 완료: {} 행", updated);
        }
    }
}