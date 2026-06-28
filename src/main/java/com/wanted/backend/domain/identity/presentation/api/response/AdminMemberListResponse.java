package com.wanted.backend.domain.identity.presentation.api.response;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "관리자 회원 목록 조회 응답")
public record AdminMemberListResponse(
        @Schema(description = "회원 목록")
        List<AdminMemberItemResponse> content,
        @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
        int page,
        @Schema(description = "페이지당 조회 수", example = "10")
        int size,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    public static AdminMemberListResponse from(AdminMemberListResult result) {
        return new AdminMemberListResponse(
                result.items().stream()
                        .map(AdminMemberItemResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.hasNext()
        );
    }
}