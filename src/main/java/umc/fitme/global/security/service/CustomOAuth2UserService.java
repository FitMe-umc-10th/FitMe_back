package umc.fitme.global.security.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import umc.fitme.domain.user.converter.UserConverter;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.enums.SocialType;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.security.dto.KakaoResponse;
import umc.fitme.global.security.dto.NaverResponse;
import umc.fitme.global.security.dto.OAuth2Response;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.exception.RequireAccountLinkException;
import umc.fitme.global.security.exception.SocialLoginException;
import umc.fitme.global.security.exception.code.SocialLoginErrorCode;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomOAuth2UserService  extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /***
     * ResourceServer로 부터 받은 userRequest를 OAuth2Response DTO로 변환.
     * 얻은 회원 정보를 DB에 저장.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 회원 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("소셜 로그인 attribute 수신, registrationId={}", userRequest.getClientRegistration().getRegistrationId());

        // 카카오, 네이버 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;

        if (registrationId.equals("kakao")){
            String providerId = String.valueOf((Long)oAuth2User.getAttribute("id"));
            Map<String, Object> attributes = oAuth2User.getAttribute("kakao_account");
            if (attributes == null) {
                throw new SocialLoginException(SocialLoginErrorCode.USER_INFO_NOT_FOUND);
            }
            Map<String, Object> profile = (Map<String, Object>) attributes.get("profile");
            oAuth2Response = new KakaoResponse(providerId, attributes.get("email").toString(), profile.get("nickname").toString());
        } else if (registrationId.equals("naver")){
            Map<String, Object> attributes = (Map<String, Object>) oAuth2User.getAttribute("response");
            if (attributes == null) {
                throw new SocialLoginException(SocialLoginErrorCode.USER_INFO_NOT_FOUND);
            }
            oAuth2Response = new NaverResponse(attributes.get("id").toString(), attributes.get("email").toString(), attributes.get("name").toString());
        } else {
            throw new SocialLoginException(SocialLoginErrorCode.PROVIDER_NOT_FOUND);
        }

        // 이메일로 기존 회원 조회
        Optional<User> optionalUser = userRepository.findByEmail(oAuth2Response.getEmail());

        // 이미 DB에 해당 이메일이 존재한다면 계정연동로직 발동
        // 만약 이메일이 존재하는데, 해당 Provider였다면 패스
        if (optionalUser.isPresent()){
            User user = optionalUser.get();

            checkEmailOverlapAndThrow(user, oAuth2Response.getProvider(), oAuth2Response.getEmail(), oAuth2Response.getProviderId());

            log.info("기존 회원 소셜 로그인 성공 (이메일: {})", user.getEmail());
            return new PrincipalDetails(user, "USER");

        } else { // DB상의 첫 이메일이라면 신규 회원가입 로직 발동
            User newUser = UserConverter.oAuthResToUser(oAuth2Response);

            userRepository.save(newUser);
            log.info("신규 소셜 회원가입 완료 (이메일: {})", oAuth2Response.getEmail());

            return new PrincipalDetails(newUser, "USER");
        }
    }

    //
    private void checkEmailOverlapAndThrow(User user, SocialType provider, String email, String providerId) {
        boolean isAlreadyLinked = false;

        // 카카오 이메일로 로그인 시도했는데, 기존 DB에 카카오ID가 저장되어 있다면 -> PASS
        if (provider == SocialType.KAKAO && user.getSocialType() == SocialType.KAKAO
            && providerId.equals(user.getSocialUid())){
            isAlreadyLinked = true;
        }
        // 네이버 이메일로 로그인 시도했는데, 기존 DB에 네이버ID가 저장되어 있다면 -> PASS
        if (provider == SocialType.NAVER && user.getSocialType() == SocialType.NAVER
            && providerId.equals(user.getSocialUid())){
            isAlreadyLinked = true;
        }

        // 일반 가입자이면 -> CONFLICT
        if (!isAlreadyLinked){
            log.warn("이메일 충돌 발생 - 기존 계정과 연동 필요. (요청 소셜: {}, 이메일: {})", provider, email);

            throw new RequireAccountLinkException(
                    "이미 다른 로그인 수단으로 가입된 이메일입니다. 기존 계정에 연동하시겠습니까?",
                    user.getId(),
                    email,
                    provider,
                    providerId
            );
        }
    }
}
