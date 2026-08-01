package umc.fitme.domain.user.converter;

import umc.fitme.domain.user.entity.User;
import umc.fitme.global.security.dto.OAuth2Response;

public class UserConverter {

    /***
     * 함수 이름: 제공받은 소셜로그인 정보를 User 객체로 변환한다.
     * @param oAuth2Response 소셜로그인 정보
     * @return User 객체
     */
    public static User oAuthResToUser(OAuth2Response oAuth2Response){
        return User.builder()
                .email(oAuth2Response.getEmail())
                .name(oAuth2Response.getName())
                .socialType(oAuth2Response.getProvider())
                .socialUid(oAuth2Response.getProviderId())
                .build();
    }
}
