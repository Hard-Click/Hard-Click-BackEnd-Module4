package com.wanted.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

    /**
     * 프로젝트 전역에서 사용할 에러 코드 모음
     */
    @Getter
    public enum ErrorCode {

        // 1. 공통 예외
        INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
        INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C003", "인증이 필요합니다."),
        FORBIDDEN(HttpStatus.FORBIDDEN, "C004", "접근 권한이 없습니다."),

        // 2. 도메인별 예외
        USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 회원입니다."),
        DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다.");

        private final HttpStatus status;
        private final String code;
        private final String message;

        ErrorCode(HttpStatus status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }
    }

