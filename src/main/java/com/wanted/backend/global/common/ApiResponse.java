package com.wanted.backend.global.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
        int httpStatus,
        String message,
        T data
) {


    // 200 OK - 조회/수정
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(new ApiResponse<>(200, message, data));
    }

    // 201 Created - 생성
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, message, data));
    }

    // 200 OK - 삭제
    public static ResponseEntity<ApiResponse<Void>> successNoContent(String message) {
        return ResponseEntity.ok(new ApiResponse<>(200, message, null));
    }


}