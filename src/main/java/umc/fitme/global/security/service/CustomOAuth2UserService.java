package umc.fitme.global.security.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.security.converter.UserConverter;
import umc.fitme.global.security.dto.KakaoResponse;
import umc.fitme.global.security.dto.NaverResponse;
import umc.fitme.global.security.dto.OAuth2Response;
import umc.fitme.global.security.entity.CustomOAuth2User;
import umc.fitme.global.security.exception.SocialLoginException;
import umc.fitme.global.security.exception.code.SocialLoginErrorCode;

import java.lang.reflect.Member;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomOAuth2UserService  extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    /***
     * ResourceServer로 부터 받은 userRequest를 OAuth2Response DTO로 변환.
     * 얻은 회원 정보를 DB에 저장.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 회원 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.debug("소셜 로그인 attribute 수신, registrationId={}", userRequest.getClientRegistration().getRegistrationId());

        // 카카오, 네이버 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;

        if (registrationId.equals("kakao")){
            String providerId = String.valueOf((Long)oAuth2User.getAttribute("id"));
            Map<String, Object> attributes = oAuth2User.getAttribute("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) attributes.get("profile");
            oAuth2Response = new KakaoResponse(providerId, attributes.get("email").toString(), profile.get("nickname").toString());
        } else if (registrationId.equals("naver")){
            Map<String, Object> attributes = (Map<String, Object>) oAuth2User.getAttribute("response");
            oAuth2Response = new NaverResponse(attributes.get("id").toString(), attributes.get("email").toString(), attributes.get("name").toString());
        } else {
            throw new SocialLoginException(SocialLoginErrorCode.PROVIDER_NOT_FOUND);
        }

        // 신규회원이면 DB에 추가
        User user = userRepository.findByEmail(oAuth2Response.getEmail()).orElseGet(() -> {
            User savedUser = userRepository.save(userConverter.convert(oAuth2Response));
            log.info("신규회원 가입 완료! DB에 적재하였습니다.");
            return savedUser;
        });

        log.info("소셜 로그인에 성공하였습니다. SecurityContext에 인증객체가 답깁니다.");
        return new CustomOAuth2User(user.getId(), "USER", user.getName());
    }
}
