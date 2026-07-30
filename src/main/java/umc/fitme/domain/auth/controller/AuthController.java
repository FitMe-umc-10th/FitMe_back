package umc.fitme.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import umc.fitme.domain.auth.dto.EmailVerificationConfirmDto;
import umc.fitme.domain.auth.dto.EmailVerificationDto;
import umc.fitme.domain.auth.dto.LoginDto;
import umc.fitme.domain.auth.dto.SignUpDto;
import umc.fitme.domain.auth.exception.code.AuthSuccessCode;
import umc.fitme.domain.auth.service.AuthService;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseSuccessCode;

@RequestMapping("/api/auth")
@Tag(name = "인증 API")
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
    public ApiResponse<EmailVerificationConfirmDto.EmailVerificationConfirmResDto> emailVerifyConfirm(
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
    @Operation(summary = "회원가입 API", description = "이메일 기반 회원가입 API")
    @PostMapping("/signup")
    public ApiResponse<SignUpDto.SignUpRes> signUp(
            @Valid @RequestBody SignUpDto.SignUpReq dto
    ){
        BaseSuccessCode successCode = AuthSuccessCode.SIGNUP_OK;
        return ApiResponse.onSuccess(successCode, authService.signUp(dto));
    }

    /**
     * 함수 기능: 이메일 폼 로그인을 진행한다.
     * @param dto
     * @return
     */
    @Operation(summary = "이메일 로그인 API", description = "이메일 로그인 API")
    @PostMapping("/login")
    public ApiResponse<LoginDto.LoginRes> login(
            @Valid @RequestBody LoginDto.LoginReq dto
    ){
        BaseSuccessCode successCode = AuthSuccessCode.LOGIN_OK;

        return ApiResponse.onSuccess(successCode, authService.login(dto));
    }

    /***
     * 함수 기능: 계정연동을 진행한다.
     * @param authorizationHeader Bearer: {linkToken}
     * @return 로그인 성공 응답
     */
    @Operation(summary = "계정 연동 API", description = "충돌이 발생한 이메일과 기존 이메일을 연동한다.")
    @PatchMapping("/link")
    public ApiResponse<LoginDto.LoginRes> linkAccount(
            @RequestHeader(name = "Authorization") String authorizationHeader
    ){
        BaseSuccessCode successCode = AuthSuccessCode.LINK_ACCOUNT_OK;
        return ApiResponse.onSuccess(successCode, authService.linkAccount(authorizationHeader));
    }
}
