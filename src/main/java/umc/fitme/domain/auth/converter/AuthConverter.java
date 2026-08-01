package umc.fitme.domain.auth.converter;

import umc.fitme.domain.auth.dto.LoginDto;

public class AuthConverter {

    public static LoginDto.LoginResultDto toLoginRes(
            Long userId,
            String email,
            String name,
            Boolean isOnboarded,
            String accessToken,
            Long accessTokenValidity,
            String refreshToken){

        // LoginDto.LoginRes.Member DTO 조립
        LoginDto.LoginRes.Member member = LoginDto.LoginRes.Member.builder()
                .memberId(userId)
                .email(email)
                .name(name)
                .isOnboarded(isOnboarded)
                .build();

        // LoginDto.LoginRes DTO 조립
        LoginDto.LoginRes loginRes = LoginDto.LoginRes.builder()
                .accessToken(accessToken)
                .tokenType("Bearer ")
                .expiresIn(accessTokenValidity/1000L)
                .member(member)
                .build();

        // 최동 LoginDto.LoginResult DTO 조립
        return LoginDto.LoginResultDto.builder()
                .loginRes(loginRes)
                .refreshToken(refreshToken)
                .build();
    }
}
