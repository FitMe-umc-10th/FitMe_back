package umc.fitme.global.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.security.entity.PrincipalDetails;
import umc.fitme.global.security.exception.SocialAccountLoginException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                        log.warn("존재하지 않는 이메일입니다: {}.", email);
                        return new UsernameNotFoundException("존재하지 않는 이메일입니다: " + email);
                });

        if (user.getPassword() == null || user.getPassword().isEmpty()){
            throw new SocialAccountLoginException("소셜 로그인으로 가입된 계정입니다. 소셜 로그인을 이용해주세요.");
        }

        return new PrincipalDetails(user, "USER");
    }
}
