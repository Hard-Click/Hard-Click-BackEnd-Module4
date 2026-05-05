package com.wanted.backend.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/*
 * 설명. MDC(Mapped Diagnostic Context)란?
 *  로깅 시스템에서 요청을 추적하기 위해 사용되는 기술로,
 *  각 요청에 대한 '고유한 식별자(traceId)'를 생성하고, 이를 로깅 시스템에 저장한다.
 *  이를 통해 요청 추적 및 디버깅을 용이하게 할 수 있다.
 *  예를 들어 동시에 서로 다른 클라이이언트로부터 동일한 요청이 들어와도,
 *  각각의 요청을 구분할 수 있도록 '고유한 식별자(traceId)'를 생성한다.
 * */

/**LoggingAspect
 * 로깅 관련 설정을 담당하는 Configuration 클래스
 * 
 * <p>
 * MDC(Mapped Diagnostic Context)를 활용한 요청 추적 설정과
 * 로깅 필터를 구성한다.
 * </p>
 */
@Configuration
public class LoggingConfig {

    /**
     * 요청별 고유 ID를 생성하고 MDC에 설정하는 필터를 Bean으로 등록한다.
     * 
     * <p>
     * 각 HTTP 요청마다 고유한 traceId를 생성하여 MDC에 저장하고,
     * 요청 처리가 완료되면 MDC를 정리한다.
     * </p>
     * 
     * @return MDC 설정 필터
     */
    @Bean
    public OncePerRequestFilter mdcLoggingFilter() {
        /*
         * 설명. OncePerRequestFilter란?
         * Spring Web에서 제공하는 필터(Filter)로, HTTP 요청(Request) 하나당 단 한 번만 실행되는 것을 보장한다.
         * 서블릿(Servlet) 필터 체인(Filter Chain) 내에서 포워드(Forward) 등이 발생하더라도 중복 실행을 방지한다.
         * 이 빈(Bean)은 모든 요청에 대해 고유한 traceId를 생성하고 MDC(Mapped Diagnostic Context)에 주입하여
         * 로그 추적 및 디버깅을 용이하게 하기 위해 등록되었다.
         */
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {

                // 요청별 고유 ID를 부여하기 위해 UUID를 생성
                String traceId = UUID.randomUUID().toString().substring(0, 8);

                try {
                    // MDC에 고유 ID를 설정
                    MDC.put("traceId", traceId);
                    // MDC에 요청 URI를 설정
                    MDC.put("requestURI", request.getRequestURI());
                    // MDC에 HTTP 메소드를 설정
                    MDC.put("method", request.getMethod());
                    // MDC에 클라이언트 IP 주소를 설정
                    MDC.put("remoteAddr", getClientIpAddress(request));

                    // 다음 필터 체인 실행
                    filterChain.doFilter(request, response);

                } finally {
                    // 요청 처리 완료 후 MDC를 정리
                    MDC.clear();
                }
            }
        };
    }

    /**
     * 클라이언트의 실제 IP 주소를 추출한다.
     * 
     * <p>
     * 프록시나 로드밸런서를 거쳐온 요청의 경우
     * X-Forwarded-For 헤더에서 실제 IP를 추출한다.
     * </p>
     * 
     * @param request HTTP 요청
     * @return 클라이언트 IP 주소
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}