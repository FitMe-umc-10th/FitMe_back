package umc.fitme.global.apiPayload.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import umc.fitme.domain.user.controller.CustomerServiceController;
import umc.fitme.domain.user.controller.UserApplicationController;
import umc.fitme.domain.user.service.FaqService;
import umc.fitme.domain.user.service.InquiryService;
import umc.fitme.domain.user.service.UserApplicationService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GeneralExceptionHandlerTest {

    private MockMvc mockMvc() {
        UserApplicationService userApplicationService = mock(UserApplicationService.class);
        return MockMvcBuilders
                .standaloneSetup(new UserApplicationController(userApplicationService))
                .setControllerAdvice(new GeneralExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("어드바이스의 @ExceptionHandler 매핑이 부모와 충돌하지 않는다 (기동 실패 방지)")
    void exceptionHandlerMappings_areNotAmbiguous() {
        // ResponseEntityExceptionHandler를 상속하면서 부모가 이미 매핑한 예외 타입을
        // @ExceptionHandler로 다시 선언하면 이 생성자가 IllegalStateException을 던지고
        // 애플리케이션 기동이 실패한다. 스프링이 기동 시 수행하는 검사와 동일한 경로다.
        assertThatCode(() -> new ExceptionHandlerMethodResolver(GeneralExceptionHandler.class))
                .doesNotThrowAnyException();
    }

    @Test
    void getUserApplications_withoutTab_returnsBadRequest() throws Exception {
        mockMvc().perform(get("/api/v1/user-applications"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드는 500이 아닌 405로 응답한다")
    void unsupportedHttpMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc().perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/user-applications"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON405_1"));
    }

    @Test
    @DisplayName("본문이 깨진 JSON은 500이 아닌 400으로 응답한다")
    void malformedJsonBody_returnsBadRequest() throws Exception {
        mockMvc().perform(post("/api/v1/user-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }

    @Test
    @DisplayName("@Valid 검증 실패는 400 과 실패한 필드별 메시지를 함께 내려준다")
    void invalidRequestBody_returnsFieldErrors() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerServiceController(
                        mock(FaqService.class), mock(InquiryService.class)))
                .setControllerAdvice(new GeneralExceptionHandler())
                .build();

        mockMvc.perform(post("/api/v1/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"replyEmail\":\"not-an-email\",\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result.replyEmail").value("이메일 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.result.content").value("문의 내용은 필수입니다."));
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type은 500이 아닌 415로 응답한다")
    void unsupportedContentType_returnsUnsupportedMediaType() throws Exception {
        mockMvc().perform(post("/api/v1/user-applications")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("postId=1"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON415_1"));
    }
}
