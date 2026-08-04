package umc.fitme.domain.user.converter;

import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.enums.SocialType;
import umc.fitme.global.security.dto.OAuth2Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserConverter {

    private static final DateTimeFormatter kakao = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter naver = DateTimeFormatter.ofPattern("yyyyMM-dd");

    /***
     * 함수 이름: 제공받은 소셜로그인 정보를 User 객체로 변환한다.
     * @param oAuth2Response 소셜로그인 정보
     * @return User 객체
     */
    public static User oAuthResToUser(OAuth2Response oAuth2Response){

        LocalDate birth = convertBirth(oAuth2Response.getProvider(), oAuth2Response.getBirthyear(), oAuth2Response.getBirthday());

        return oAuth2Response.getProvider() == SocialType.KAKAO?
                    User.builder()
                        .email(oAuth2Response.getEmail())
                        .name(oAuth2Response.getName())
                        .kakaoId(oAuth2Response.getProviderId())
                        .birth(birth)
                        .build() :
                    User.builder()
                        .email(oAuth2Response.getEmail())
                        .name(oAuth2Response.getName())
                        .naverId(oAuth2Response.getProviderId())
                        .birth(birth)
                        .build();
    }

    private static LocalDate convertBirth(SocialType provider, String birthyear, String birthday) {
        if (provider == SocialType.KAKAO){
            String dateStr = birthyear + birthday;
            return LocalDate.parse(dateStr, kakao);
        } else {
            String dateStr = birthyear + birthday;
            return LocalDate.parse(dateStr, naver);
        }
    }
}
