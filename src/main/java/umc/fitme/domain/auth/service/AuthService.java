package umc.fitme.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.auth.dto.EmailVerificationConfirmDto;
import umc.fitme.domain.auth.dto.EmailVerificationDto;
import umc.fitme.domain.auth.dto.SignUpDto;
import umc.fitme.domain.auth.entity.EmailVerification;
import umc.fitme.domain.auth.exception.AuthException;
import umc.fitme.domain.auth.exception.code.AuthErrorCode;
import umc.fitme.domain.user.exception.UserException;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.EmailVerificationRepository;
import umc.fitme.domain.user.repository.UserRepository;

import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final long CODE_TTL_SECONDS = 300L;

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSender emailSender;

    private final SecureRandom secureRandom = new SecureRandom(); // 6자리 난수 생성

    /***
     * 함수 기능: 요청된 이메일로 6자리 인증번호가 발송된다.
     * @param dto 이메일
     * @return 인증번호, 만료시간(5분) dto
     */
    public EmailVerificationDto.EmailVerificationResDto sendVerificationCode(EmailVerificationDto.EmailVerificationReqDto dto) {
        String email = dto.email();

        // 이미 가입된 이메일로 인증을 할 경우, "이미 가입된 이메일입니다" 반환
        if (userRepository.existsByEmail(email)){
           throw new UserException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String code = generateCode(); // 6자리 인증 코드 생성

        EmailVerification emailVerification = EmailVerification.create(email, code, CODE_TTL_SECONDS);
        emailVerificationRepository.save(emailVerification);

        emailSender.sendVerificationCode(email, code);

        return new EmailVerificationDto.EmailVerificationResDto(email, CODE_TTL_SECONDS);
    }

    /***
     * 함수 기능: 난수 6자리 생성
     * @return 상동
     */
    private String generateCode() {
        int number = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    /***
     * 함수 기능: 사용자가 입력한 6자리 인증번호를 검증한다.
     * @param dto 이메일, 인증번호(6자리)
     * @return 이메일, isVerified t/f dto
     */
    public EmailVerificationConfirmDto.EmailVerificationConfirmResDto isValidateCode(EmailVerificationConfirmDto.EmailVerificationConfirmReqDto dto){

        // 이메일 확인
        EmailVerification emailVerification = emailVerificationRepository.findTopByEmailOrderByIdDesc(dto.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.EMAIL_NOT_FOUND));

        // 인증번호 만료 시 에러
        if (emailVerification.isExpired()){
            throw new AuthException(AuthErrorCode.CODE_NOT_VALIDATE);
        }

        // 인증번호가 잘못되었을 시 에러
        if (!emailVerification.matches(dto.verificationCode())){
            throw new AuthException(AuthErrorCode.CODE_NOT_MATCH);
        }

        emailVerification.verify();

        return EmailVerificationConfirmDto.EmailVerificationConfirmResDto.builder()
                .email(dto.email())
                .isVerified(true)
                .build();
    }

    public SignUpDto.SignUpRes signUp(SignUpDto.SignUpReq dto) {

        // 이메일 인증 여부 검증
        EmailVerification emailVerification = emailVerificationRepository.findTopByEmailOrderByIdDesc(dto.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.EMAIL_NOT_FOUND));
        if (emailVerification.getVerifiedAt() == null){
            throw new AuthException(AuthErrorCode.NEED_TO_VERIFY);
        }

        // 비밀번호 일치 검증
        if (!dto.password().equals(dto.passwordConfirm())){
            throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        // 개인정보 약관 동의 여부 검증
        if (!dto.privacyPolicyAgreed()){
            throw new AuthException(AuthErrorCode.NEED_TO_AGREE);
        }

        // 비밀번호 암호화 후 DB에 저장 로직
        
    }
}
