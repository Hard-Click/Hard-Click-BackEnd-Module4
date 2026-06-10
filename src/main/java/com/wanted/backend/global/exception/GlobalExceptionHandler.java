package com.wanted.backend.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * [1차 방어선] @Valid 유효성 검사 실패 예외 처리
     * 클라이언트가 값을 잘못 보낸 경우 (WARN 레벨)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        // 에러가 난 필드와 원인을 Map에 담습니다
        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        log.warn("[Validation Error] Path: {}, Message: 클라이언트 입력값 오류", request.getRequestURI());

        ErrorResponse response = ErrorResponse.create()
                .errorCode(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message(ErrorCode.INVALID_INPUT_VALUE.getMessage())
                .path(request.getRequestURI())
                .details(details);

        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(response);
    }

    /**
     * [2차 방어선] 도메인 규칙 위반 예외 처리
     * 도메인 모델(Course 등)이 던지는 IllegalArgumentException (WARN 레벨)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        log.warn("[Domain Validation Error] Path: {}, Message: {}", request.getRequestURI(), e.getMessage());

        ErrorResponse response = ErrorResponse.create()
                .errorCode(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message(e.getMessage())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * [3차 방어선] 비즈니스 로직 예외 처리
     * throw new BusinessException(...) 으로 던진 예외 (WARN 레벨)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request) {

        ErrorCode errorCode = e.getErrorCode();

        log.warn("[Business Error] Code: {}, Message: {}", errorCode.getCode(), errorCode.getMessage());

        ErrorResponse response = ErrorResponse.create()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(request.getRequestURI());

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /**
     * [최종 방어선] 우리가 예상하지 못한 모든 시스템 장애
     * 500 에러는 ERROR 레벨로 찍어 error.log 파일로 직행시킵니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(
            Exception e,
            HttpServletRequest request) {

        log.error("[System Error] Path: {}, Message: {}", request.getRequestURI(), e.getMessage(), e);

        ErrorResponse response = ErrorResponse.create()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}