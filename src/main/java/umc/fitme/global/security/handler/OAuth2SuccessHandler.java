package umc.fitme.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.exception.UserException;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;
import umc.fitme.global.security.dto.LoginResDto;
import umc.fitme.global.security.entity.CustomOAuth2User;
import umc.fitme.global.security.exception.code.SocialLoginSuccessCode;
import umc.fitme.global.security.util.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /***
     * 함수 기능: 로그인 성공 시, accessToken/refreshToken 발급 후 응답
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

        log.info("발급된 accessToken: {}", accessToken);
        log.info("발급된 refreshToken: {}", refreshToken);

        // 소셜 로그인 성공 시, 반환되는 응답 DTO
        createLoginResDto(response, userId, name, accessToken, refreshToken);
    }

    /***
     * 함수 기능: 생성된 accessToken, refreshToken과 함께 유저 정보 및 온보딩 여부도 담아 응답한다.
     * @param response 응답
     * @param userId 유저ID
     * @param name 유저이름
     * @param accessToken
     * @param refreshToken
     * @throws IOException
     */
    private void createLoginResDto(HttpServletResponse response, Long userId, String name, String accessToken, String refreshToken) throws IOException {

        // 온보딩 여부 조사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        Boolean isOnboarded = user.getIsOnboarded();

        // LoginResDto.UserInfo 응답 DTO 빌드
        LoginResDto.UserInfo userInfo = LoginResDto.UserInfo.builder()
                .userId(userId)
                .name(name)
                .isOnboarded(isOnboarded)
                .build();

        // ObjectMapper 생성
        ObjectMapper objectMapper = new ObjectMapper();
        BaseSuccessCode successCode = SocialLoginSuccessCode.SOCIAL_LOGIN_SUCCESS;

        // 전체 응답 DTO 생성 (LoginResDto)
        ApiResponse<LoginResDto> responseBody = ApiResponse.onSuccess(successCode, LoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfo(userInfo)
                .build());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(successCode.getStatus().value());
        // 응답 반환
        objectMapper.writeValue(response.getOutputStream(), responseBody);
    }
}
