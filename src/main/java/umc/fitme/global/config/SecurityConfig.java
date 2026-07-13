package umc.fitme.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import umc.fitme.global.security.exception.CustomAccessDenied;
import umc.fitme.global.security.exception.CustomEntryPoint;
import umc.fitme.global.security.filter.JwtAuthenticationFilter;
import umc.fitme.global.security.handler.OAuth2FailureHandler;
import umc.fitme.global.security.handler.OAuth2SuccessHandler;
import umc.fitme.global.security.service.CustomOAuth2UserService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomEntryPoint customEntryPoint;
    private final CustomAccessDenied customAccessDenied;

    private final String[] authenticatedUris = {
            "/api/v1/user-applications",
            "/api/v1/user-applications/**"
    };

    private final String[] allowUris = {
        "/",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/error/**",
        "/api/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2Login((oauth2) -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(authenticatedUris).authenticated()
                        .requestMatchers(allowUris).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutSuccessUrl("/"))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customEntryPoint) // 401 UNAUTHORIZED
                        .accessDeniedHandler(customAccessDenied)); // 403 FORBIDDEN

        return http.build();
    }
}
