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
        DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
        INVALID_LOGIN_INFO(HttpStatus.UNAUTHORIZED, "U003", "이메일 또는 비밀번호가 올바르지 않습니다."),
        ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "U004", "비밀번호를 5회 연속 틀려 계정이 잠겼습니다."),
        INSTRUCTOR_NOT_FOUND(HttpStatus.UNAUTHORIZED, "U005", "제공받은 계정 정보가 존재하지 않습니다."),
        DUPLICATE_USERNAME(HttpStatus.CONFLICT, "U006", "이미 사용 중인 아이디입니다."),
        FORBIDDEN_USERNAME(HttpStatus.BAD_REQUEST, "U007", "사용할 수 없는 아이디입니다."),
        PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "U008", "비밀번호가 일치하지 않습니다."),
        VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "U009", "유효하지 않은 인증코드입니다. 다시 입력해주세요."),
        EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U010", "이메일 전송에 실패했습니다. 다시 시도해주세요."),
        PASSWORD_RESET_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "U011", "비밀번호 재발급은 하루 최대 3회까지만 가능합니다."),
        MULTIPLE_LOGIN_DETECTED(HttpStatus.UNAUTHORIZED, "U012", "동일 계정의 동시 로그인이 감지되어 재로그인이 필요합니다."),

        // 3. 강의(Course) 예외
        COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "CR001", "존재하지 않는 강의입니다."),
        INVALID_COURSE_PRICE(HttpStatus.BAD_REQUEST, "CR002", "강의 가격 설정이 올바르지 않습니다."),
        LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "CR003", "존재하지 않는 회차입니다."),

        // 리뷰 도메인
        REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "R001", "이미 리뷰를 작성한 강의입니다."),
        REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R002", "존재하지 않는 리뷰입니다."),
        REVIEW_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "R003", "본인의 리뷰만 수정/삭제할 수 있습니다."),
        NOT_ENROLLED(HttpStatus.FORBIDDEN, "R004", "수강 중인 강의만 리뷰를 작성할 수 있습니다.");

        private final HttpStatus status;
        private final String code;
        private final String message;

        ErrorCode(HttpStatus status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }
    }

