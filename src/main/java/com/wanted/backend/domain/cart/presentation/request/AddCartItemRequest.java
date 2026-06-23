package com.wanted.backend.domain.cart.presentation.request;

import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull(message = "강의 ID는 필수입니다.")
        Long courseId
) {}
