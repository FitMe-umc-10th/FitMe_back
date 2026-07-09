package umc.fitme.global.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import umc.fitme.global.security.entity.CustomUserDetails;
import umc.fitme.global.security.exception.SocialLoginException;
import umc.fitme.global.security.exception.code.SocialLoginErrorCode;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    /***
     * JwtUtil 생성자
     * @param secretKey 고유 JWT 시크릿 키
     * @param accessTokenValidity accessToken 유효시간
     * @param refreshTokenValidity refreshToken 유효시간
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    /***
     * 유저ID, role, 유저이름을 기반으로 AccessToken을 생성한다.
     * @param userId DB에 저장된 유저ID
     * @param role 유저 역할 (USER로 고정)
     * @param name 유저 이름
     * @return accessToken
     */
    public String createAccessToken(Long userId, String role, String name){

        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("typ", "access")
                .claim("role", role)
                .claim("name", name)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /***
     * 유저ID를 기반으로 RefreshToken을 생성하고 반환한다.
     * @param userId DB에 저장된 유저ID
     * @return refreshToken
     */
    public String createRefreshToken(Long userId){

        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenValidity);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("typ", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /***
     * 사용자의 토큰에 대한 유효성을 검증한다.
     * @param token 사용자가 보유한 JWT 토큰
     * @return true/false
     */
    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .clockSkewSeconds(60)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("토큰이 유효하지 않습니다. {}", e.getMessage());
            throw new SocialLoginException(SocialLoginErrorCode.TOKEN_NOT_VALIDATE);
        }
    }

    /***
     * 토큰의 Payload에서 정보를 꺼내어 인증 객체를 조립한다.
     * @param token 사용자가 보유한 JWT 토큰
     * @return 인증 객체
     */
    public Authentication getAuthentication(String token){

        Claims payload = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long userId = Long.parseLong(payload.getSubject());
        String role = payload.get("role", String.class);
        String name = payload.get("name", String.class);

        CustomUserDetails principal = new CustomUserDetails(userId, role, name);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}


