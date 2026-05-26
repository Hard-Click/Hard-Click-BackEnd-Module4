package com.wanted.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

    /**
     * 프로젝트 전역에서 사용할 에러 코드 모음
     */
    @Getter
    public enum ErrorCode {

    /* =========================================================================
       1. 공통 예외 (C000)
       ========================================================================= */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C003", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C004", "접근 권한이 없습니다."),

    /* =========================================================================
       2. 회원 및 계정 관련 예외 (U000)
       ========================================================================= */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "가입되지 않은 이메일입니다."), // (명세서 9번 문구 반영)
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    INVALID_LOGIN_INFO(HttpStatus.UNAUTHORIZED, "U003", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "U004", "비밀번호를 5회 연속 틀려 계정이 잠겼습니다."),
    INSTRUCTOR_NOT_FOUND(HttpStatus.UNAUTHORIZED, "U005", "제공받은 계정 정보가 존재하지 않습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "U006", "이미 사용 중인 아이디입니다."),
    FORBIDDEN_USERNAME(HttpStatus.BAD_REQUEST, "U007", "사용할 수 없는 아이디입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "U008", "비밀번호가 일치하지 않습니다."),
    VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "U009", "유효하지 않은 인증코드입니다. 다시 입력해주세요."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U010", "이메일 전송에 실패했습니다. 다시 시도해주세요."),
    PASSWORD_RESET_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "U011", "비밀번호 재발급은 하루 최대 3회까지만 가능합니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "U013", "이메일 인증이 완료되지 않았습니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "U014", "유효하지 않은 권한입니다."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "U016", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."),
        INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_009", "현재 비밀번호가 일치하지 않습니다."),

    /* =========================================================================
       3. 인증 및 보안 관련 고도화 예외 (AUTH000)
       ========================================================================= */
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_002", "인증 요청 내역이 없습니다."),
    VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_003", "인증번호가 올바르지 않습니다."),
    VERIFICATION_EXPIRED(HttpStatus.GONE, "AUTH_004", "인증번호가 만료되었습니다."),
    ALREADY_REGISTERED_EMAIL(HttpStatus.CONFLICT, "AUTH_005", "이미 가입된 이메일입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_006", "유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.GONE, "AUTH_007", "Refresh Token이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND , "AUTH_008", "저장된 Refresh Token이 없습니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_009", "현재 비밀번호가 일치하지 않습니다."), // (명세서 16번 반영)

    /* =========================================================================
       4. 강의(Course) 관련 예외 (CR000)
       ========================================================================= */
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "CR001", "존재하지 않는 강의입니다."),
    INVALID_COURSE_PRICE(HttpStatus.BAD_REQUEST, "CR002", "강의 가격 설정이 올바르지 않습니다."),
    LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "CR003", "존재하지 않는 회차입니다."),
    COURSE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CR004", "해당 강의에 대한 권한이 없습니다."),

    /* =========================================================================
       5. 리뷰 도메인 관련 예외 (R000)
       ========================================================================= */
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "R001", "이미 리뷰를 작성한 강의입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R002", "존재하지 않는 리뷰입니다."),
    REVIEW_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "R003", "본인의 리뷰만 수정/삭제할 수 있습니다."),
    NOT_ENROLLED(HttpStatus.FORBIDDEN, "R004", "수강 중인 강의만 리뷰를 작성할 수 있습니다."),

    /* =========================================================================
       6. 수강신청 및 학습활동 관련 예외 (EN/L000)
       ========================================================================= */
    ENROLLMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "EN001", "이미 수강 중인 강의입니다."),
    VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "존재하지 않는 영상입니다."),
    COURSE_NOT_PUBLISHED(HttpStatus.FORBIDDEN, "L002", "공개되지 않은 강의입니다."),
    ENROLLMENT_REQUIRED(HttpStatus.FORBIDDEN, "L003", "수강권 또는 구독권이 필요합니다."),
    VIDEO_COMPLETION_CONDITION_NOT_MET(HttpStatus.BAD_REQUEST, "L004", "영상 시청 비율이 완료 기준을 충족하지 않습니다."),

    //게시글 예외
    SUBJECT_REQUIRED(HttpStatus.BAD_REQUEST, "P001", "질문게시판은 과목 선택이 필수입니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "존재하지 않는 게시글입니다."),
    FILE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "P002", "파일은 최대 2개까지 첨부 가능합니다."),
    POST_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "P004", "본인의 게시글만 수정/삭제할 수 있습니다."),
    POST_ACCEPTED_CANNOT_DELETE(HttpStatus.FORBIDDEN, "P005", "채택 완료된 게시글은 삭제할 수 없습니다."),
    POST_ACCEPTED_CANNOT_MODIFY(HttpStatus.FORBIDDEN, "P006", "채택 완료된 게시글은 수정할 수 없습니다."),

    // 이미지 파일 업로드 예외
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F001", "jpg, jpeg, png 형식의 파일만 업로드 가능합니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F002", "파일 크기는 5MB 이하여야 합니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F003", "파일 업로드에 실패했습니다."),
    PROFILE_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "F004", "프로필 이미지는 1개만 업로드 가능합니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
