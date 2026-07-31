package umc.fitme.global.security.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    // RT 쿠키 생성
    public String createRefreshTokenCookie(String refreshToken, boolean keepLogin){

        long maxAge = keepLogin ? refreshTokenValidity/1000L : -1L;

        return ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(maxAge) // 14일 유효
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build()
                .toString();
    }
}
