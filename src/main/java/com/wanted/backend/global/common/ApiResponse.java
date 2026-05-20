package com.wanted.backend.global.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ApiResponse<T> {

    private final int httpStatus;
    private final String message;
    private final T data;

    private ApiResponse(int httpStatus, String message, T data) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.data = data;
    }

    // 200 OK - 조회/수정
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", data));
    }

    // 201 Created - 생성
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "SUCCESS", data));
    }

    // 200 OK - 삭제
    public static ResponseEntity<ApiResponse<Void>> successNoContent() {
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", null));
    }
}