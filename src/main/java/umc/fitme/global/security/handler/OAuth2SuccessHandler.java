package umc.fitme.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import umc.fitme.domain.auth.service.TokenService;
import umc.fitme.domain.user.entity.User;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.util.CookieUtil;
import umc.fitme.global.security.util.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final TokenService tokenService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUrl;
    /***
     * 함수 기능: 로그인 성공 시, accessToken/refreshToken 발급 후 리다이렉트
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     * the authentication process.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // SecurityContext에서 인증객체의 principal 가져오기
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

        // 토큰 생성 후 쿠키에 저장
        User user = principal.getUser();
        String accessToken = jwtUtil.createAccessToken(user.getId(), principal.getRole(), user.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        log.info("토큰 발급 완료 - userId: {}", user.getId());

        // 발급한 refreshToken 저장 or 업데이트
        tokenService.saveOrUpdateRefreshToken(user, refreshToken);

        // 소셜 로그인 성공 시, 프론트 주소로 리다이렉션
        redirect(request, response, user, accessToken, refreshToken);
    }

    /***
     * 함수 기능: 생성된 accessToken, refreshToken과 함께 유저 정보 및 온보딩 여부도 담아 전달.
     * @param response 응답
     * @param user 회원 객체
     * @param accessToken
     * @param refreshToken
     * @throws IOException
     */
    private void redirect(
            HttpServletRequest request,
            HttpServletResponse response,
            User user,
            String accessToken,
            String refreshToken) throws IOException {

        // 온보딩 여부 조사
        Boolean isOnboarded = user.getIsOnboarded();

        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(refreshToken, true));

        // 프론트 URL로 리다이렉트 주소 조립
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("isOnboarded", isOnboarded)
                .build()
                .encode()
                .toUriString();
        targetUrl = targetUrl + "`#accessToken`=" + accessToken;
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
