package umc.fitme.global.security.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.global.security.util.CookieUtil;
import umc.fitme.global.security.util.JwtUtil;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${jwt.secret}")
    private String secretKey;

    @Test
    @DisplayName("만료된 AT를 들고오면 401이 아닌, RT 타입 오류를 발급한다. 즉, mvc단 까지 접근한다.")
    void 만료AT_새토큰() throws Exception {

        // given
        String expiredAT = createExpiredAT();
        String rt = createRT();

        Cookie cookie = new Cookie("refreshToken", rt);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                    .header("Authorization", "Bearer " + expiredAT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(cookie))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("토큰 타입을 확인해주세요. 타입은 refresh만 가능합니다."));
    }

    private String createExpiredAT(){
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        Date now = new Date();
        Date expiredAt = new Date(now.getTime() - (60 * 60 * 1000));

        return Jwts.builder()
                .subject(String.valueOf(1L))
                .expiration(expiredAt)
                .issuedAt(now)
                .signWith(key)
                .compact();
    }

    private String createRT(){
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + (60 * 60 * 1000));

        return Jwts.builder()
                .subject(String.valueOf(1L))
                .expiration(expiredAt)
                .issuedAt(now)
                .signWith(key)
                .compact();
    }
}
