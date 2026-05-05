package com.wanted.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 초기 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (Postman 테스트 시 필수)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. CORS 설정 비활성화 (프론트엔드와 연결 시 에러 방지)
                .cors(AbstractHttpConfigurer::disable)

                // 3. 모든 요청에 대해 무조건 승인 (Full Pass)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // 4. 기본 로그인 폼 및 HTTP Basic 인증 비활성화 (Postman 팝업 방지)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}