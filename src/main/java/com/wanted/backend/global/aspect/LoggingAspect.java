package com.wanted.backend.global.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP를 활용한 로깅 처리 Aspect
 * 
 * <p>서비스 계층의 메서드 실행 전후에 로그를 자동으로 기록한다.
 * 메서드 실행 시간, 매개변수, 반환값, 예외 정보를 포함한다.</p>
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * 서비스 계층의 모든 메서드에 대한 포인트컷을 정의한다.
     */
    @Pointcut("execution(* com.wanted.backend..application.service..*(..))")
    public void serviceLayer() {}

    /**
     * 컨트롤러 계층의 모든 메서드에 대한 포인트컷을 정의한다.
     */
    @Pointcut("execution(* com.wanted.backend..presentation..*(..))")
    public void controllerLayer() {}

    /**
     * 메서드 실행 전에 로그를 기록한다.
     * 
     * @param joinPoint 조인포인트 정보
     */
    @Before("serviceLayer() || controllerLayer()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.info("==> {}#{} 실행 시작 - 매개변수: {}", 
                className, methodName, Arrays.toString(args));
    }

    /**
     * 메서드 정상 실행 후에 로그를 기록한다.
     * 
     * @param joinPoint 조인포인트 정보
     * @param result 메서드 반환값
     */
    @AfterReturning(pointcut = "serviceLayer() || controllerLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.info("<== {}#{} 실행 완료 - 반환값: {}", 
                className, methodName, result);
    }

    /**
     * 메서드 실행 중 예외 발생 시 로그를 기록한다.
     * 
     * @param joinPoint 조인포인트 정보
     * @param exception 발생한 예외
     */
    @AfterThrowing(pointcut = "serviceLayer() || controllerLayer()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.error("!!! {}#{} 실행 중 예외 발생 - 예외: {}, 메시지: {}", 
                className, methodName, exception.getClass().getSimpleName(), exception.getMessage());
    }

    /**
     * 메서드 실행 시간을 측정하고 로그를 기록한다.
     * 
     * @param proceedingJoinPoint 프로시딩 조인포인트
     * @return 메서드 실행 결과
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around("serviceLayer()")
    public Object logExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String className = proceedingJoinPoint.getTarget().getClass().getSimpleName();
        String methodName = proceedingJoinPoint.getSignature().getName();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = proceedingJoinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            log.info("{}#{} 실행 시간: {}ms", className, methodName, executionTime);
            
            // 성능 경고 (1초 이상 소요 시)
            if (executionTime > 1000) {
                log.warn("[s2][LoggingAspect] {}#{} 실행 시간이 {}ms로 느립니다. 성능 최적화가 필요합니다.", 
                        className, methodName, executionTime);
            }
            
            return result;
            
        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            log.error("{}#{} 실행 실패 - 실행 시간: {}ms, 예외: {}",
                    className, methodName, executionTime, throwable.getMessage());
            
            throw throwable;
        }
    }

    /**
     * 비즈니스 이벤트 로깅을 위한 포인트컷
     * 대출, 반납 등 중요한 비즈니스 로직에 대해 상세한 로그를 기록한다.
     */
//    @Pointcut("execution(* com.wanted.backend..LoanService.createLoan(..))" +
//            "|| execution(* com.wanted.backend..LoanService.returnBook(..))" +
//            "|| execution(* com.wanted.backend..LoanService.extendLoan(..))")
//    public void businessEvents() {}
//
//    /**
//     * 중요한 비즈니스 이벤트에 대한 상세 로깅을 수행한다.
//     *
//     * @param proceedingJoinPoint 프로시딩 조인포인트
//     * @return 메서드 실행 결과
//     * @throws Throwable 메서드 실행 중 발생한 예외
//     */
//    @Around("businessEvents()")
//    public Object logBusinessEvent(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        String methodName = proceedingJoinPoint.getSignature().getName();
//        Object[] args = proceedingJoinPoint.getArgs();
//
//        log.info("비즈니스 이벤트 시작 - 작업: {}, 매개변수: {}", methodName, Arrays.toString(args));
//
//        try {
//            Object result = proceedingJoinPoint.proceed();
//
//            log.info("비즈니스 이벤트 성공 - 작업: {}, 결과: {}", methodName, result);
//
//            return result;
//
//        } catch (Throwable throwable) {
//            log.error("비즈니스 이벤트 실패 - 작업: {}, 예외: {}, 메시지: {}",
//                    methodName, throwable.getClass().getSimpleName(), throwable.getMessage());
//
//            throw throwable;
//        }
//    }
} 