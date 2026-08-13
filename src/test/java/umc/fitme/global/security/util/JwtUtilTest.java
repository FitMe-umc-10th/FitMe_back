package umc.fitme.global.security.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import umc.fitme.domain.auth.repository.BlacklistRepository;
import umc.fitme.domain.user.enums.SocialType;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.security.exception.code.TokenErrorCode;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import umc.fitme.global.apiPayload.exception.ProjectException;

class JwtUtilTest {

    private static final String SECRET = "fitme-test-secret-key-must-be-at-least-256-bits";
    private static final long AT_VALIDITY = 3_600_000L;
    private static final long RT_VALIDITY = 86_400_000L;

    private UserRepository userRepository;
    private BlacklistRepository blacklistRepository;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        blacklistRepository = mock(BlacklistRepository.class);
        jwtUtil = new JwtUtil(SECRET, AT_VALIDITY, RT_VALIDITY, userRepository, blacklistRepository);
    }

    private SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("validateLinkToken - LT 검증")
    class ValidateLinkToken {

        @Test
        @DisplayName("Bearer 형식이 아니면 LT_INVALID 예외를 던진다")
        void Bearer_형식이_아니면_LT_INVALID() {
            assertThatThrownBy(() -> jwtUtil.validateLinkToken("linkToken"))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(TokenErrorCode.LT_INVALID);
        }

        @Test
        @DisplayName("서명이 위조된 토큰은 LT_EXPIRED가 아닌 LT_INVALID 예외를 던진다")
        void 서명이_위조된_토큰은_LT_INVALID() {
            // given: 다른 키로 서명한 토큰
            given(blacklistRepository.findByToken(anyString())).willReturn(Optional.empty());
            String forged = Jwts.builder()
                    .subject("1")
                    .claim("typ", "link")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 180_000))
                    .signWith(key("another-secret-key-must-be-at-least-256-bits!!"))
                    .compact();

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateLinkToken("Bearer " + forged))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(TokenErrorCode.LT_INVALID);
        }

        @Test
        @DisplayName("형식이 깨진 토큰은 LT_INVALID 예외를 던진다")
        void 형식이_깨진_토큰은_LT_INVALID() {
            given(blacklistRepository.findByToken(anyString())).willReturn(Optional.empty());

            assertThatThrownBy(() -> jwtUtil.validateLinkToken("Bearer not-a-jwt"))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(TokenErrorCode.LT_INVALID);
        }

        @Test
        @DisplayName("만료된 토큰은 LT_EXPIRED 예외를 던진다")
        void 만료된_토큰은_LT_EXPIRED() {
            // given: clockSkew(60초)보다 충분히 이전에 만료된 토큰
            given(blacklistRepository.findByToken(anyString())).willReturn(Optional.empty());
            long now = System.currentTimeMillis();
            String expired = Jwts.builder()
                    .subject("1")
                    .claim("typ", "link")
                    .issuedAt(new Date(now - 600_000))
                    .expiration(new Date(now - 300_000))
                    .signWith(key(SECRET))
                    .compact();

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateLinkToken("Bearer " + expired))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(TokenErrorCode.LT_EXPIRED);
        }

        @Test
        @DisplayName("블랙리스트에 등록된 토큰은 사유를 덮어쓰지 않고 AT_BLACKLISTED를 그대로 전달한다")
        void 블랙리스트_토큰은_AT_BLACKLISTED_그대로() {
            // given
            String linkToken = jwtUtil.createLinkToken(1L, "fitme@example.com", SocialType.KAKAO, "kakao-1");
            given(blacklistRepository.findByToken(linkToken))
                    .willReturn(Optional.of(mock(umc.fitme.domain.auth.entity.Blacklist.class)));

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateLinkToken("Bearer " + linkToken))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(TokenErrorCode.AT_BLACKLISTED);
        }

        @Test
        @DisplayName("정상 LT는 토큰 문자열을 그대로 반환한다")
        void 정상_LT는_토큰을_반환한다() {
            // given
            String linkToken = jwtUtil.createLinkToken(1L, "fitme@example.com", SocialType.KAKAO, "kakao-1");
            given(blacklistRepository.findByToken(linkToken)).willReturn(Optional.empty());

            // when & then
            assertThat(jwtUtil.validateLinkToken("Bearer " + linkToken)).isEqualTo(linkToken);
        }
    }

    @Nested
    @DisplayName("TokenErrorCode 코드 체계")
    class TokenErrorCodes {

        @Test
        @DisplayName("에러 코드 문자열은 중복되지 않는다")
        void 코드_문자열은_중복되지_않는다() {
            assertThat(TokenErrorCode.values())
                    .extracting(TokenErrorCode::getCode)
                    .doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("코드 문자열의 상태값과 HTTP 상태가 일치한다")
        void 코드의_상태값과_HTTP_상태가_일치한다() {
            for (TokenErrorCode errorCode : TokenErrorCode.values()) {
                // 예: "TOKEN401_5" -> 401
                String statusInCode = errorCode.getCode().replaceAll("\\D*(\\d{3}).*", "$1");

                assertThat(String.valueOf(errorCode.getStatus().value()))
                        .as("%s 의 코드(%s)와 HTTP 상태(%d)", errorCode.name(),
                                errorCode.getCode(), errorCode.getStatus().value())
                        .isEqualTo(statusInCode);
            }
        }
    }
}
