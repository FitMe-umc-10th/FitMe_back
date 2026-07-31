package umc.fitme.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
                .ifPresentOrElse(
                        existingToken -> {
                            existingToken.updateToken(refreshToken, expiresAt);
                        },
                        () -> {
                            RefreshToken newRefreshToken = RefreshToken.builder()
                                    .user(user)
                                    .token(refreshToken)
                                    .expiresAt(expiresAt)
                                    .build();
                            refreshTokenRepository.save(newRefreshToken);
                        }
                );
    }
}