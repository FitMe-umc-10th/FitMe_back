package umc.fitme.global.security.filter;

import jakarta.servlet.ServletException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp(){
        jwtAuthenticationFilter = new JwtAuthenticationFilter(null);
    }

    @Test
    @DisplayName("AT 토큰이 필요하지 않는 API는 shouldNotFilter가 true를 반환해야 한다.")
    void 특정API_필터제외_테스트() throws ServletException {
        // given
        String[] excludePaths = {
                "/api/auth/signup",
                "/api/auth/login",
                "/api/auth/email-verifications",
                "/api/auth/link",
                "/api/auth/reissue"
        };

        for (String path: excludePaths){
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI(path);

            // when
            boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

            // then
            assertThat(result).as("경로: " + path).isTrue();
        }
    }
}