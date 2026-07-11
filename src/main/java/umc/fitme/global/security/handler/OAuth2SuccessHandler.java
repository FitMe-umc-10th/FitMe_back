package umc.fitme.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.exception.UserException;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.security.dto.LoginResDto;
import umc.fitme.global.security.entity.CustomOAuth2User;
import umc.fitme.global.security.util.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

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
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();

        Long userId = principal.getUserId();
        String role = principal.getRole();
        String name = principal.getName();

        String accessToken = jwtUtil.createAccessToken(userId, role, name);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        log.info("토큰 발급 완료 - userId: {}", userId);

        // 소셜 로그인 성공 시, 프론트 주소로 리다이렉션
        redirect(request, response, userId, name, accessToken, refreshToken);
    }

    /***
     * 함수 기능: 생성된 accessToken, refreshToken과 함께 유저 정보 및 온보딩 여부도 담아 전달.
     * @param response 응답
     * @param userId 유저ID
     * @param name 유저이름
     * @param accessToken
     * @param refreshToken
     * @throws IOException
     */
    private void redirect(
            HttpServletRequest request,
            HttpServletResponse response,
            Long userId, String name, String accessToken, String refreshToken) throws IOException {

        // 온보딩 여부 조사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        Boolean isOnboarded = user.getIsOnboarded();

        // Refresh Token은 보안상 쿠키로 전달
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(1209600)
                .path("/")
//                .secure(true) // 배포 시 주석 해제 (HTTPS 환경)
//                .sameSite("None") // 배포 시 주석 해제 (CORS)
                .httpOnly(true)
                .build();

        response.addHeader("Set-Cookie", cookie.toString() );

        // 프론트 URL로 리다이렉트 주소 조립
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3030/oauth2/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("userId", userId)
                .queryParam("name", name)
                .queryParam("isOnboarded", isOnboarded)
                .build()
                .encode()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
