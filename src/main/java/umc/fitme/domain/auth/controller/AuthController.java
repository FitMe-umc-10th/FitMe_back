package umc.fitme.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.fitme.domain.auth.dto.EmailVerificationDto;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/email-verifications")
    public ApiResponse<Void> emailVerify(
            @Valid @RequestBody EmailVerificationDto.EmailVerificationReqDto email
            ){
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }
}
