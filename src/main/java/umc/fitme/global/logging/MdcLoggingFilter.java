package umc.fitme.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/***
 * 필터 기능: 모든 요청의 최외곽에서 MDC 컨텍스트를 세팅하고, 요청당 액세스 로그 1줄을 남긴다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class MdcLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    public static final String TRACE_ID_HEADER = "X-Request-Id";
    public static final String CLIENT_IP = "X-Real-IP";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 시작시간
        long startedAt = System.currentTimeMillis();

        // 클라이언트가 준 ID가 있으면 이어받고, 없으면 새로 발급
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)){
            traceId = UUID.randomUUID().toString().substring(0,8);
        }

        // MDC 컨텍스트에 아래 정보들을 삽입
        MDC.put(TRACE_ID, traceId);
        MDC.put("httpMethod", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        String clientIp = request.getHeader(CLIENT_IP);
        MDC.put("clientIp", StringUtils.hasText(clientIp) ? clientIp : "unknown");

        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 걸린 시간 = 현재 시간 - 시작 시간
            long took = System.currentTimeMillis() - startedAt;
            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("latencyMs", String.valueOf(took));

            log.info("{} {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), took);
            MDC.clear();
        }
    }
}
