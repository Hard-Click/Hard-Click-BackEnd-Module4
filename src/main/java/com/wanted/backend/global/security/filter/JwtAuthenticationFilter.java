package com.wanted.backend.global.security.filter;

import com.wanted.backend.global.security.CustomUserDetails;
import com.wanted.backend.global.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
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
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        String token = resolveToken(bearerToken);

        // 2. 토큰 유효성 검사 및 인증 객체 설정
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {

            if ("access".equals(jwtProvider.getTokenType(token))) {
                Long memberId = jwtProvider.getMemberIdFromToken(token);
                String role = jwtProvider.getRoleFromToken(token);


                String username = jwtProvider.getUsernameFromToken(token);

                // CustomUserDetails를 Principal로 설정하여 @AuthenticationPrincipal 사용 가능하게 함
                CustomUserDetails userDetails = new CustomUserDetails(
                        memberId,
                        username,
                        "",
                        false, // JWT 인증이 완료된 상태이므로 기본적으로 잠기지 않은 것으로 간주
                        true,  // 활성화된 상태로 간주
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