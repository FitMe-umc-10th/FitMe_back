package umc.fitme.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import umc.fitme.domain.auth.dto.EmailVerificationDto;
import umc.fitme.domain.user.exception.UserException;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public EmailVerificationDto.EmailVerificationResDto sendEmailVerification(EmailVerificationDto.EmailVerificationReqDto dto) {
        String email = dto.email();

        // 이미 가입된 이메일로 인증을 할 경우, "이미 가입된 이메일입니다" 반환
        if (userRepository.existsByEmail(email)){
           throw new UserException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }


    }
}
