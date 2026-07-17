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
import umc.fitme.domain.auth.dto.SignUpDto;
import umc.fitme.domain.auth.exception.code.AuthSuccessCode;
import umc.fitme.domain.auth.service.AuthService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;
import umc.fitme.global.apiPayload.code.GeneralSuccessCode;

@RequestMapping("/api/auth")
@Tag(name = "이메일 인증 / 이메일 기반 회원가입 / 로그인(소셜x) 관련 API")
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
    @Operation(summary = "이메일 인증 요청 API", description = "이메일로 인증 번호를 보내는 API")
    @PostMapping("/email-verifications")
    public ApiResponse<EmailVerificationDto.EmailVerificationResDto> emailVerify(
            @Valid @RequestBody EmailVerificationDto.EmailVerificationReqDto dto
    ){
        BaseSuccessCode successCode = AuthSuccessCode.REQUEST_OK;
        return ApiResponse.onSuccess(successCode, authService.sendVerificationCode(dto));
    }

    /***
     * 함수 기능: 사용자가 이메일로 전달받은 인증번호를 검증한다.
     * @param confirm 사용자 이메일 및 인증번호
     * @return 공통응답형식
     */
    @Operation(summary = "이메일 인증 확인 API", description = "이메일 인증 번호를 검증하는 API")
    @PostMapping("/email-verifications/confirm")
    public ApiResponse<EmailVerificationConfirmDto.EmailVerificationConfirmResDto> emailVerityConfirm(
            @Valid @RequestBody EmailVerificationConfirmDto.EmailVerificationConfirmReqDto confirm
    ){
        BaseSuccessCode successCode = AuthSuccessCode.CONFIRM_OK;
        return ApiResponse.onSuccess(successCode, authService.isValidateCode(confirm));
    }

    /***
     * 함수 기능: 이메일 기반 회원가입을 진행한다.
     * @param dto 회원가입 정보
     * @return
     */
    @PostMapping("/signup")
    public ApiResponse<SignUpDto.SignUpRes> signUp(
            @Valid @RequestBody SignUpDto.SignUpReq dto
    ){
        BaseSuccessCode successCode = GeneralSuccessCode.OK;
        return ApiResponse.onSuccess(successCode, authService.signUp(dto));
    }
}
