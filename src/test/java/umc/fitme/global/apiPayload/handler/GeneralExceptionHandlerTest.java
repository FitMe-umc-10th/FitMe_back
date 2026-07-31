package umc.fitme.global.apiPayload.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import umc.fitme.global.apiPayload.ApiResponse;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralExceptionHandlerTest {

    private final GeneralExceptionHandler handler = new GeneralExceptionHandler();

    @Test
    void handleMissingServletRequestParameterException_returnsBadRequest() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("tab", "String");

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleMissingServletRequestParameterException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo("COMMON400_1");
    }
}
