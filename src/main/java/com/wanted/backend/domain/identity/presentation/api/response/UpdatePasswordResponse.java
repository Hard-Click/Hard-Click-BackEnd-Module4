package com.wanted.backend.domain.identity.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 응답")
public record UpdatePasswordResponse(
        @Schema(description = "비밀번호가 변경된 회원 ID", example = "1")
        Long memberId
) {
}