package umc.fitme.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;

import java.io.IOException;

/***
 * 401 UNAUTHORIZED 에러 핸들러
 */
@Component
public class CustomEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        // 응답DTO 내려주기
        ObjectMapper objectMapper = new ObjectMapper();
        BaseErrorCode errorCode = GeneralErrorCode.UNAUTHORIZED;

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getStatus().value());

        ApiResponse<Object> errorResponse = ApiResponse.onFailure(errorCode, null);

        // objectMapper.writeValue(B, A)
        // A를 JSON으로 바꿔, B라는 통로에 쏜다.
        // errorResponse 객체를 JSON으로 변경함과 동시에, 중간에 메모리를 낭비하지 않고
        // 프론트엔드로 가는 응답 통로(outputstream)에 쏟아붓는다.
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
