package umc.fitme.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.exception.TokenException;
import umc.fitme.global.security.exception.code.TokenErrorCode;
import umc.fitme.global.security.util.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    /***
     * 함수 기능: excludePath 내 API 경로는 JwtAuthentication 필터를 거치지 않는다.
     * @param request current HTTP request
     * @return
     * @throws ServletException
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String[] excludePath = {
                "/api/auth/signup",
                "/api/auth/login",
                "/api/auth/demo-token",
                "/api/auth/email-verifications",
                "/api/auth/link",
                "/api/auth/reissue"
        };

        for (String pattern : excludePath){
            if (pathMatcher.match(pattern, request.getRequestURI())){
                return true; // JwtAuthenticationFilter 건너뜀
            }
        }
        return false;
    }

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

        // token의 헤더가 Authorization인데, AT 검증에서 문제가 생기면 catch절로 건너뜀.
        try {
            // token의 헤더가 Authorization이면 if 구문 실행
            // token의 헤더가 Link-Token이거나 없다면 다음 필터로 넘어간다.
            if (token != null){
                jwtUtil.validateToken(token);

                Authentication authentication = jwtUtil.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
                MDC.put("userId", String.valueOf(principal.getUser().getId()));

                // 회원탈퇴, 로그아웃 시 해당 AT를 블랙리스트에 추가하기 위해서
                request.setAttribute("accessToken", token);
            }
        } catch (ExpiredJwtException e) {
            // AT가 만료된 경우
            log.warn("AT가 만료되었습니다. {}", e.getMessage());
            BaseErrorCode tokenExpired = TokenErrorCode.AT_EXPIRED;
            setErrorResponse(response, tokenExpired);
            return;
        } catch (JwtException | IllegalArgumentException e){
            log.warn("유효하지 않은 토큰입니다. {}", e.getMessage());
            BaseErrorCode tokenInvalid = TokenErrorCode.AT_INVALID;
            setErrorResponse(response, tokenInvalid);
            return;
        } catch (TokenException e){
            log.warn("에러 코드: {}, 에러 메시지: {}", e.getErrorCode(), e.getMessage());
            setErrorResponse(response, e.getErrorCode());
            return;
        }


        filterChain.doFilter(request, response);
    }

     // 헤더에서 토큰 추출
    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    // AT가 만료되었거나, 유효하지 않은 경우, 해당 함수의 에러형식에 맞게 반환
    private void setErrorResponse(HttpServletResponse response, BaseErrorCode errorCode) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getStatus().value());

        ApiResponse<Object> errorResponse = ApiResponse.onFailure(errorCode, null);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
