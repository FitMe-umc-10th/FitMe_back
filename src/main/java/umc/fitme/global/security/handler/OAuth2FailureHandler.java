package umc.fitme.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import umc.fitme.global.security.exception.RequireAccountLinkException;
import umc.fitme.global.security.exception.SocialLoginException;
import umc.fitme.global.security.util.JwtUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.failure-redirection-uri}")
    private String failureRedirectUrl;

    private final JwtUtil jwtUtil;

    /***
     * 함수 기능: 소셜 로그인 실패 시 에러 경로로 리다이렉트
     * @param request the request during which the authentication attempt occurred.
     * @param response the response.
     * @param exception the exception which was thrown to reject the authentication
     * request.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        log.info("OAuth2 로그인 실패 원인: {}", exception.getMessage());
        if (exception instanceof RequireAccountLinkException ex){
            String linkToken = jwtUtil.createLinkToken(ex.getUserId(), ex.getEmail(), ex.getProvider(), ex.getProviderId());

            String targetUrl = UriComponentsBuilder.fromUriString(failureRedirectUrl)
                    .queryParam("error", "REQUIRE_LINK")
                    .queryParam("message", ex.getMessage())
                    .encode(StandardCharsets.UTF_8)
                    .build().toUriString();

            targetUrl = targetUrl + "`#linkToken'=" + linkToken;
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

            log.info("linkToken 전송 완료");
        } else if (exception instanceof SocialLoginException ex){
            String targetUrl = UriComponentsBuilder.fromUriString(failureRedirectUrl)
                    .queryParam("error", ex.getErrorCode().getCode())
                    .queryParam("message", ex.getErrorCode().getMessage())
                    .encode(StandardCharsets.UTF_8)
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            getRedirectStrategy().sendRedirect(request, response, failureRedirectUrl+"?error=UNKNOWN");
        }
    }
}
