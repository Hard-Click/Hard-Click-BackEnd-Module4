package com.wanted.backend.domain.identity.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "중복 확인 응답")
public record DuplicateCheckResponse(
        @Schema(description = "중복 여부", example = "false")
        boolean exists
) {
}