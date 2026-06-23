package com.wanted.backend.domain.cart.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @Schema(description = "장바구니에 담을 강의 ID", example = "1")
        @NotNull(message = "강의 ID는 필수입니다.")
        Long courseId
) {}
