package umc.fitme.global.apiPayload.handler;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import umc.fitme.domain.user.controller.UserApplicationController;
import umc.fitme.domain.user.service.UserApplicationService;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GeneralExceptionHandlerTest {

    @Test
    void getUserApplications_withoutTab_returnsBadRequest() throws Exception {
        UserApplicationService userApplicationService = mock(UserApplicationService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserApplicationController(userApplicationService))
                .setControllerAdvice(new GeneralExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/user-applications"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }
}
