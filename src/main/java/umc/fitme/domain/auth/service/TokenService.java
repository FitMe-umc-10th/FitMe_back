package umc.fitme.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.auth.entity.RefreshToken;
import umc.fitme.domain.auth.repository.RefreshTokenRepository;
import umc.fitme.domain.user.entity.User;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-validity}") private Long refreshTokenValidity;
    /**
     * 함수 기능: RT를 재발급합니다
     * @param user
     * @param refreshToken
     */
    public void saveOrUpdateRefreshToken(User user, String refreshToken) {

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenValidity / 1000L);

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse( // 해당 RT가 DB에 있다면, 값 업데이트
                        existingToken -> {
                            existingToken.updateToken(refreshToken, expiresAt);
                        },
                        () -> { // 해당 RT가 DB에 없다면, 새로 추가
                            RefreshToken newRefreshToken = RefreshToken.builder()
                                    .user(user)
                                    .token(refreshToken)
                                    .expiresAt(expiresAt)
                                    .build();
                            refreshTokenRepository.save(newRefreshToken);
                        }
                );
    }

    /***
     * 함수 기능: 해킹으로 의심되는 RT를 삭제시킨다.
     * Propagation.REQUIRES_NEW: 기존 트랜잭션을 잠시 멈추고 새로운 독립적인 물리 트랜잭션을 생성하는 전파 속성
     * @param token RT
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteCompromisedToken(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }
}