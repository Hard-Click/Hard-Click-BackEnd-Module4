package com.wanted.backend.global.security.filter;

import com.wanted.backend.global.security.CustomUserDetails;
import com.wanted.backend.global.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        String token = resolveToken(bearerToken);

        // 2. 토큰 유효성 검사 및 인증 객체 설정
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {

            // Access Token 인지 확인 (Refresh Token으로 접근하는 것 방지)
            if ("access".equals(jwtProvider.getTokenType(token))) {
                Long memberId = jwtProvider.getMemberIdFromToken(token);
                String role = jwtProvider.getRoleFromToken(token);

                // CustomUserDetails를 Principal로 설정하여 @AuthenticationPrincipal 사용 가능하게 함
                CustomUserDetails userDetails = new CustomUserDetails(
                        memberId,
                        null, // 필터 단계에서는 로그용 외에 이메일이 필수적이지 않음
                        "",   // 비밀번호는 보안상 빈 값
                        List.of(new SimpleGrantedAuthority(role))
                );

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}