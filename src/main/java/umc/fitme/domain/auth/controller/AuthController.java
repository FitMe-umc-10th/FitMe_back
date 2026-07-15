package umc.fitme.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.fitme.domain.auth.dto.EmailVerificationConfirmDto;
import umc.fitme.domain.auth.dto.EmailVerificationDto;
import umc.fitme.domain.auth.service.AuthService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;

@RequestMapping("/api/auth")
@Tag(name = "인증")
@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /***
     * 함수 기능: 전달받은 이메일로 6자리 난수를 생성하고, 해당 이메일로 인증번호를 발송한다.
     * @param dto 사용자 이메일
     * @return 공통응답형식
     */
    @Operation(summary = "인증번호 발송")
    @PostMapping("/email-verifications")
    public ApiResponse<EmailVerificationDto.EmailVerificationResDto> emailVerify(
            @Valid @RequestBody EmailVerificationDto.EmailVerificationReqDto dto
            ){
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, authService.sendEmailVerification(dto));
    }

    /***
     * 함수 기능: 사용자가 이메일로 전달받은 인증번호를 검증한다.
     * @param confirm 사용자 이메일 및 인증번호
     * @return 공통응답형식
     */
    @Operation(summary = "인증번호 검증")
    @PostMapping("/email-verifications/confirm")
    public ApiResponse<EmailVerificationConfirmDto.EmailVerificationConfirmResDto> emailVerityConfirm(
            @Valid @RequestBody EmailVerificationConfirmDto.EmailVerificationConfirmReqDto confirm
    ){

        BaseSuccessCode successCode = GeneralSuccessCode.OK;
        return ApiResponse.onSuccess(successCode, null);
    }
}
