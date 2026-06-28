package com.wanted.backend.domain.identity.presentation.api.response;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 회원 목록 항목")
public record AdminMemberItemResponse(
        @Schema(description = "회원 ID", example = "1")
        Long memberId,
        @Schema(description = "아이디", example = "testuser")
        String username,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        @Schema(description = "역할 (STUDENT, INSTRUCTOR, ADMIN)", example = "STUDENT")
        String role,
        @Schema(description = "상태 (ACTIVE, SUSPENDED, WITHDRAWN)", example = "ACTIVE")
        String status,
        @Schema(description = "신고 누적 횟수", example = "2")
        int reportCount,
        @Schema(description = "가입 일시", example = "2025-01-15T09:00:00")
        LocalDateTime createdAt,
        @Schema(description = "마지막 로그인 일시", example = "2026-06-01T13:30:00")
        LocalDateTime lastLoginAt
) {
    public static AdminMemberItemResponse from(AdminMemberListResult.Item item) {
        return new AdminMemberItemResponse(
                item.memberId(),
                item.username(),
                item.name(),
                item.email(),
                item.role().name(),
                item.status().name(),
                item.reportCount(),
                item.createdAt(),
                item.lastLoginAt()
        );
    }
}