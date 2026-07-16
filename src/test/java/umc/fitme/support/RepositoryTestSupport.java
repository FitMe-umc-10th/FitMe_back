package umc.fitme.support;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @DataJpaTest 기반 리포지토리 테스트 공통 베이스.
 * <p>
 * H2로 DataSource를 대체하지 않고(application-test.yml에 설정한) 실제 MySQL
 * 테스트 스키마(fitme_test)에 접속한다. 리포지토리 테스트는 이 클래스를 상속한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public abstract class RepositoryTestSupport {
}