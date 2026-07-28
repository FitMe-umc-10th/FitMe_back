package umc.fitme.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;

import java.io.IOException;

/**
 * 403 FORBIDDEN 예외 핸들러
 */
@Component
public class CustomAccessDenied implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 응답DTO 내려주기
        ObjectMapper objectMapper = new ObjectMapper();
        BaseErrorCode errorcode = GeneralErrorCode.FORBIDDEN;

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorcode.getStatus().value());

        ApiResponse<Object> errorResponse = ApiResponse.onFailure(errorcode, null);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
