
package umc.fitme.global.security.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/***
 * 소셜 로그인으로만 가입된 계정에 이메일/비밀번호 로그인을 시도한 경우 발생한다.
 *
 * UserDetailsService 규약상 조회 실패는 UsernameNotFoundException 계열이어야 하므로 이를 상속하되,
 * "가입되지 않은 이메일"과 응답 코드를 구분하기 위해 별도 타입으로 둔다.
 */
public class SocialAccountLoginException extends UsernameNotFoundException {

    public SocialAccountLoginException(String msg) {
        super(msg);
    }
}
