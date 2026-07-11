package umc.fitme.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import umc.fitme.global.security.util.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /***
     * 1. Request Header에서 token 추출
     * 2. 추출한 토큰이 유효한지 검증
     * 3. 유효하다면 Authentication 객체 생성
     * 4. SecurityContext에 Authentication 저장
     * 5. doFilter를 통해 다음 필터로 이동
     * @param request 요청
     * @param response 응답
     * @param filterChain 필터체인
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtUtil.validateToken(token)){

            Authentication authentication = jwtUtil.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("SecurityContext에 Authentication 객체 저장완료: {}", authentication.getPrincipal());
        }

        filterChain.doFilter(request, response);
    }

    /***
     * 헤더에서 토큰 추출
     * @param request 요청
     * @return 토큰
     */
    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
