package umc.fitme.domain.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.auth.converter.AuthConverter;
import umc.fitme.domain.auth.dto.*;
import umc.fitme.domain.auth.entity.Blacklist;
import umc.fitme.domain.auth.entity.EmailVerification;
import umc.fitme.domain.auth.entity.RefreshToken;
import umc.fitme.domain.auth.exception.AuthException;
import umc.fitme.domain.auth.exception.code.AuthErrorCode;
import umc.fitme.domain.auth.repository.BlacklistRepository;
import umc.fitme.domain.auth.repository.EmailVerificationRepository;
import umc.fitme.domain.auth.repository.RefreshTokenRepository;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.enums.SocialType;
import umc.fitme.domain.user.exception.UserException;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.exception.TokenException;
import umc.fitme.global.security.exception.code.TokenErrorCode;
import umc.fitme.global.security.util.JwtUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final long CODE_TTL_SECONDS = 300L;
    private final SecureRandom secureRandom = new SecureRandom(); // 6자리 난수 생성

    @Value("${jwt.access-token-validity}")
    private Long accessTokenValidity;

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

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
     * 함수 기능: 사용자가 입력한 6자리 인증번호를 검증한다.
     * @param dto 이메일, 인증번호(6자리)
     * @return 이메일, isVerified t/f dto
     */
    public EmailVerificationConfirmDto.EmailVerificationConfirmResDto isValidateCode(EmailVerificationConfirmDto.EmailVerificationConfirmReqDto dto){

        // 이메일 확인
        EmailVerification emailVerification = emailVerificationRepository.findTopByEmailOrderByIdDesc(dto.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.EMAIL_CODE_NOT_FOUND));

        // 인증 완료 코드 재사용 방지
        if (emailVerification.getVerifiedAt() != null){
            throw new AuthException(AuthErrorCode.CODE_ALREADY_USED);
        }

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

    /***
     * 함수 기능: 이메일 기반 회원가입을 진행한다.
     * @param dto 회원가입 시 필요한 정보
     * @return 가입한 이메일과 시간
     */
    public SignUpDto.SignUpRes signUp(SignUpDto.SignUpReq dto) {

        // 이미 가입된 이메일로 회원가입을 시도 할 경우, "이미 가입된 이메일입니다" 반환
        if (userRepository.existsByEmail(dto.email())){
            throw new UserException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 이메일 인증 여부 검증
        EmailVerification emailVerification = emailVerificationRepository.findTopByEmailOrderByIdDesc(dto.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.EMAIL_CODE_NOT_FOUND));
        if (emailVerification.getVerifiedAt() == null){
            throw new AuthException(AuthErrorCode.NEED_TO_VERIFY);
        }

        // 일회용 인증코드가 이미 사용되었다면 "이미 사용된 코드입니다" 반환
        if (emailVerification.isUsed()){
            throw new AuthException(AuthErrorCode.CODE_ALREADY_USED);
        }

        // 만료 시간 검증 (인증 완료된지 30분 후에 회원가입을 진행하면 "이메일 인증 시간이 초과" 반환
        if (emailVerification.getVerifiedAt().plusMinutes(30).isBefore(LocalDateTime.now())){
            throw new AuthException(AuthErrorCode.VERIFICATION_EXPIRED);
        }

        // 해당 인증 번호 사용처리
        emailVerification.consume();

        // 비밀번호 암호화 후 DB에 저장 로직
        String encode = passwordEncoder.encode(dto.password());

        userRepository.save(User.builder()
                .name(dto.name())
                .email(dto.email())
                .birth(dto.birth())
                .password(encode)
                .termsAgreed(true)
                .build());

        return SignUpDto.SignUpRes.builder()
                .email(dto.email())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /***
     * 함수 기능: 이메일 기반 로그인을 진행한다.
     * @param dto 이메일, 비번, 로그인 유지 여부
     * @return accessToken, refreshToken, 유저 정보
     */
    public LoginDto.LoginResultDto login(LoginDto.LoginReq dto) {

        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

            Long userId = principal.getUser().getId();
            String role = principal.getRole();
            String email = principal.getUsername();

            String accessToken = jwtUtil.createAccessToken(userId, role, email);
            String refreshToken = jwtUtil.createRefreshToken(userId);

            // 생성된 RT를 DB에 저장/업데이트 합니다.
            tokenService.saveOrUpdateRefreshToken(principal.getUser(), refreshToken);

            return AuthConverter.toLoginRes(
                    userId,
                    email,
                    principal.getUser().getName(),
                    principal.getUser().getIsOnboarded(),
                    accessToken,
                    accessTokenValidity,
                    refreshToken);

        } catch (DisabledException e) {
            throw new AuthException(AuthErrorCode.DELETED_USER_EMAIL);
        } catch (BadCredentialsException e){
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }
    }

    /***
     * 함수 기능: 1. 헤더의 authorizationHeader안의 linkToken을 추출한다.
     *           2. linkToken을 검증한다.
     *           3. 토큰 안에 있는 정보를 바탕으로 기존 이메일에 새로운 정보를 추가한다.
     *           4. 로그인을 진행하고 AT, RT를 발급하여 반환한다.
     * @param authorizationHeader Bearer: {linkToken}
     * @return LoginDto.LoginRes 로그인 성공 응답
     */
    public LoginDto.LoginResultDto linkAccount(String authorizationHeader) {

        // LT 검증
        String linkToken = jwtUtil.validateLinkToken(authorizationHeader);

        // LT 정보 추출
        LinkTokenDto linkTokenInfo = jwtUtil.getLinkTokenInfo(linkToken);

        // LT안의 UserId가 DB에 존재하지 않으면 예외 리턴
        User user = userRepository.findById(linkTokenInfo.getUserId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 계정 연동 진행
        user.linkAccount(SocialType.valueOf(linkTokenInfo.getSocialType()), linkTokenInfo.getProviderId());

        // 계정 연동 후 AT, RT 발급 후 반환
        String accessToken = jwtUtil.createAccessToken(user.getId(), "USER", user.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        log.info("계정 연동 완료 후 AT, RT 발급완료");

        return AuthConverter.toLoginRes(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getIsOnboarded(),
                accessToken,
                accessTokenValidity,
                refreshToken);
    }

    /***
     * 함수 기능: RT 검증 및 AT, RT 재발급 (RTR 방식)
     * @param refreshToken
     * @return
     */
    public TokenInfoDto.TokenInfoRes reissue(String refreshToken) {

        // RT가 null이면 에러 리턴
        if (refreshToken == null){
            throw new TokenException(TokenErrorCode.RT_NOT_FOUND);
        }
        try { // RT가 유효하지 않다면 예외 리턴
            jwtUtil.validateToken(refreshToken);
        } catch (ExpiredJwtException e){
            throw new TokenException(TokenErrorCode.RT_EXPIRED);
        } catch (JwtException e){
            throw new TokenException(TokenErrorCode.RT_INVALID);
        }

        // 유저 정보 추출
        Long userId = jwtUtil.getUserIdFromRT(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // DB에 저장된 RT와 일치하는지 검증 (RTR 보안 방어)
        RefreshToken dbToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new TokenException(TokenErrorCode.RT_INVALID));
        if (!dbToken.getToken().equals(refreshToken)){ // [해킹 의심 상황] RT 삭제
            tokenService.deleteCompromisedToken(dbToken);
            throw new TokenException(TokenErrorCode.RT_INVALID);
        }

        String newAccessToken = jwtUtil.createAccessToken(userId, "USER", user.getEmail());
        String newRefreshToken = jwtUtil.createRefreshToken(userId);

        // RefreshToken 테이블에 업데이트
        tokenService.saveOrUpdateRefreshToken(user, newRefreshToken);

        return TokenInfoDto.TokenInfoRes.builder()
                .info(TokenInfoDto.ATInfo.builder().accessToken(newAccessToken).build())
                .refreshToken(newRefreshToken)
                .build();
    }

    /***
     * 함수 기능: 로그아웃. AT를 블랙리스트로 등록하고, RT는 삭제한다.
     * @param userId
     * @param accessToken
     */
    public void logout(Long userId, String accessToken) {

        // AT 블랙리스트 추가 & RT 삭제 (있다면)
        addATBlacklistAndDeleteRT(userId, accessToken);
    }

    /***
     * 함수 기능: 회원탈퇴. AT를 블랙리스트로 등록하고, RT는 삭제한다.
     *          deleted_at 컬럼을 추가하고, 이메일도 더미데이터로 변경한다.
     * @param userId
     * @param accessToken
     */
    public void deleteUser(Long userId, String accessToken) {

        // AT 블랙리스트 추가 & RT 삭제 (있다면)
        User user = addATBlacklistAndDeleteRT(userId, accessToken);

        // deleted_at 컬럼에 시간 추가 및 이메일 값을 더미 데이터로 덮어씌움
        user.deleteUser();
    }

    /***
     * 함수 기능: 임시 토큰 발급 (데모데이 전까지 유지)
     * @return AT
     */
    @Transactional(readOnly = true)
    public TokenInfoDto.ATInfo getDemoToken() {

        String accessToken = jwtUtil.createAccessToken(1L, "USER", "test@example.com");

        return TokenInfoDto.ATInfo.builder()
                .accessToken(accessToken)
                .build();
    }

    // 이메일 인증번호를 위한 6자리 난수 생성
    private String generateCode() {
        int number = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private User addATBlacklistAndDeleteRT(Long userId, String accessToken) {
        // 회원 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // AT 블랙리스트 등록
        blacklistRepository.save(new Blacklist(accessToken));

        // RT 삭제
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        return user;
    }
}
