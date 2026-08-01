package umc.fitme.global.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import umc.fitme.domain.auth.dto.LinkTokenDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.enums.SocialType;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.exception.TokenException;
import umc.fitme.global.security.exception.code.TokenErrorCode;

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
     * @param email 유저 이메일
     * @return accessToken
     */
    public String createAccessToken(Long userId, String role, String email){

        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("typ", "access")
                .claim("role", role)
                .claim("email", email)
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
     * 함수 기능: 계정 연동을 위한 linkToken
     * @param email
     * @param userId
     * @return
     */
    public String createLinkToken(Long userId, String email, SocialType provider, String providerId) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + 180000); // 3분

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("typ", "link")
                .claim("email", email)
                .claim("provider", provider.toString())
                .claim("providerId", providerId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /***
     * 사용자의 토큰에 대한 유효성을 검증한다.
     * @param token 사용자가 보유한 JWT 토큰
     */
    public void validateToken(String token){
        Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(60)
                .build()
                .parseSignedClaims(token);
    }

    /***
     * 함수 기능: 헤더에 담겨온 LT를 추출하고 검증한다.
     * @param linkTokenHeader
     * @return
     */
    public String validateLinkToken(String linkTokenHeader) {
        if (linkTokenHeader == null || !linkTokenHeader.startsWith("Bearer ")){
            throw new TokenException(TokenErrorCode.LT_INVALID);
        }

        String linkToken = linkTokenHeader.substring(7);

        try {
            validateToken(linkToken);
        } catch (Exception e){
            throw new TokenException(TokenErrorCode.LT_EXPIRED);
        }
        return linkToken;
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
        String email = payload.get("email", String.class);
        String typ = payload.get("typ", String.class);

        // 토큰 타입이 access가 아닌 경우 예외 처리
        if (!"access".equals(typ)) {
            throw new TokenException(TokenErrorCode.AT_TYPE_INVALID);
        }

        User user = User.builder()
                .id(userId)
                .email(email)
                .build();

        PrincipalDetails principal = new PrincipalDetails(user, role);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    /***
     * 함수 기능: RT에서 userId 값을 추출한다.
     * @param token RT
     * @return 회원 ID
     */
    public Long getUserIdFromRT(String token) {
        Claims payload = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String typ = payload.get("typ", String.class);
        if(!"refresh".equals(typ)){
            throw new TokenException(TokenErrorCode.RT_TYPE_INVALID);
        }

        return Long.parseLong(payload.getSubject());
    }

    /***
     * 함수 기능: LT에서 정보를 추출한다.
     * @param linkToken LT
     * @return LinkTokenDto 유저ID, 이메일, 공급자, 공급자ID
     */
    public LinkTokenDto getLinkTokenInfo(String linkToken){
        Claims payload = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(linkToken)
                .getPayload();

        String typ = payload.get("typ", String.class);
        if (!"link".equals(typ)){
            throw new TokenException(TokenErrorCode.LT_TYPE_INVALID);
        }

        return LinkTokenDto.builder()
                .userId(Long.parseLong(payload.getSubject()))
                .email(payload.get("email", String.class))
                .socialType(payload.get("provider", String.class))
                .providerId(payload.get("providerId", String.class))
                .build();
    }
}


