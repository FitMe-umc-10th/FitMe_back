package umc.fitme.domain.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.auth.dto.EmailVerificationConfirmDto;
import umc.fitme.domain.auth.dto.EmailVerificationDto;
import umc.fitme.domain.auth.entity.EmailVerification;
import umc.fitme.domain.auth.exception.AuthException;
import umc.fitme.domain.auth.exception.code.AuthErrorCode;
import umc.fitme.domain.user.exception.UserException;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.auth.repository.EmailVerificationRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL = "fitme@example.com";
    private static final String CORRECT_CODE = "123456";
    private static final String WRONG_CODE = "999999";
    private static final long TTL_SECONDS = 300L;

    @Nested
    @DisplayName("sendVerificationCode - 이메일 인증번호 발송")
    class SendVerificationCode {

        @Test
        @DisplayName("이미 가입된 이메일이면 EMAIL_ALREADY_EXISTS 예외를 던진다")
        void 이미_가입된_이메일이면_예외를_던진다() {
            // given
            EmailVerificationDto.EmailVerificationReqDto request =
                    new EmailVerificationDto.EmailVerificationReqDto(EMAIL);
            given(userRepository.existsByEmail(EMAIL)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.sendVerificationCode(request))
                    .isInstanceOf(UserException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS);

            // 저장/발송 로직은 호출되지 않아야 한다
            verify(emailVerificationRepository, never()).save(any());
            verify(emailSender, never()).sendVerificationCode(anyString(), anyString());
        }

        @Test
        @DisplayName("정상 요청 시 EmailVerification을 저장하고 메일을 발송한다")
        void 정상_요청_시_저장하고_메일을_발송한다() {
            // given
            EmailVerificationDto.EmailVerificationReqDto request =
                    new EmailVerificationDto.EmailVerificationReqDto(EMAIL);
            given(userRepository.existsByEmail(EMAIL)).willReturn(false);

            // when
            EmailVerificationDto.EmailVerificationResDto response =
                    authService.sendVerificationCode(request);

            // then - 저장된 엔티티 캡처
            ArgumentCaptor<EmailVerification> captor =
                    ArgumentCaptor.forClass(EmailVerification.class);
            verify(emailVerificationRepository, times(1)).save(captor.capture());
            EmailVerification saved = captor.getValue();

            assertThat(saved.getEmail()).isEqualTo(EMAIL);
            assertThat(saved.getVerificationCode()).hasSize(6).matches("\\d{6}");
            assertThat(saved.getVerifiedAt()).isNull();
            assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());

            // then - 메일 발송 시 저장된 코드와 동일한 코드가 사용되어야 한다
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailSender, times(1))
                    .sendVerificationCode(emailCaptor.capture(), codeCaptor.capture());
            assertThat(emailCaptor.getValue()).isEqualTo(EMAIL);
            assertThat(codeCaptor.getValue()).isEqualTo(saved.getVerificationCode());

            // then - 응답 검증
            assertThat(response.email()).isEqualTo(EMAIL);
            assertThat(response.expiresInSeconds()).isEqualTo(TTL_SECONDS);
        }
    }

    @Nested
    @DisplayName("isValidateCode - 이메일 인증번호 검증")
    class IsValidateCode {

        @Test
        @DisplayName("해당 이메일로 발급된 인증번호가 없으면 EMAIL_NOT_FOUND 예외를 던진다")
        void 인증번호가_없으면_예외를_던진다() {
            // given
            EmailVerificationConfirmDto.EmailVerificationConfirmReqDto request =
                    new EmailVerificationConfirmDto.EmailVerificationConfirmReqDto(EMAIL, CORRECT_CODE);
            given(emailVerificationRepository.findTopByEmailOrderByIdDesc(EMAIL))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.isValidateCode(request))
                    .isInstanceOf(AuthException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(AuthErrorCode.EMAIL_NOT_FOUND);
        }

        @Test
        @DisplayName("인증번호가 만료되었으면 CODE_NOT_VALIDATE 예외를 던진다")
        void 인증번호가_만료되면_예외를_던진다() {
            // given: 이미 만료된 인증번호 (ttl = -1초 → 즉시 만료)
            EmailVerification expired = EmailVerification.create(EMAIL, CORRECT_CODE, -1L);
            EmailVerificationConfirmDto.EmailVerificationConfirmReqDto request =
                    new EmailVerificationConfirmDto.EmailVerificationConfirmReqDto(EMAIL, CORRECT_CODE);

            given(emailVerificationRepository.findTopByEmailOrderByIdDesc(EMAIL))
                    .willReturn(Optional.of(expired));

            // when & then
            assertThatThrownBy(() -> authService.isValidateCode(request))
                    .isInstanceOf(AuthException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(AuthErrorCode.CODE_NOT_VALIDATE);

            // 검증 실패했으므로 verifiedAt은 여전히 null이어야 한다
            assertThat(expired.getVerifiedAt()).isNull();
        }

        @Test
        @DisplayName("인증번호가 일치하지 않으면 CODE_NOT_MATCH 예외를 던진다")
        void 인증번호가_일치하지_않으면_예외를_던진다() {
            // given
            EmailVerification valid = EmailVerification.create(EMAIL, CORRECT_CODE, TTL_SECONDS);
            EmailVerificationConfirmDto.EmailVerificationConfirmReqDto request =
                    new EmailVerificationConfirmDto.EmailVerificationConfirmReqDto(EMAIL, WRONG_CODE);

            given(emailVerificationRepository.findTopByEmailOrderByIdDesc(EMAIL))
                    .willReturn(Optional.of(valid));

            // when & then
            assertThatThrownBy(() -> authService.isValidateCode(request))
                    .isInstanceOf(AuthException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(AuthErrorCode.CODE_NOT_MATCH);

            assertThat(valid.getVerifiedAt()).isNull();
        }

        @Test
        @DisplayName("정상 인증 시 verify()가 호출되고 isVerified=true 응답을 반환한다")
        void 정상_인증_시_인증완료_처리된다() {
            // given
            EmailVerification valid = EmailVerification.create(EMAIL, CORRECT_CODE, TTL_SECONDS);
            EmailVerificationConfirmDto.EmailVerificationConfirmReqDto request =
                    new EmailVerificationConfirmDto.EmailVerificationConfirmReqDto(EMAIL, CORRECT_CODE);

            given(emailVerificationRepository.findTopByEmailOrderByIdDesc(EMAIL))
                    .willReturn(Optional.of(valid));

            // when
            EmailVerificationConfirmDto.EmailVerificationConfirmResDto response =
                    authService.isValidateCode(request);

            // then
            assertThat(response.email()).isEqualTo(EMAIL);
            assertThat(response.isVerified()).isTrue();
            assertThat(valid.getVerifiedAt()).isNotNull(); // verify() 호출 확인
        }
    }
}