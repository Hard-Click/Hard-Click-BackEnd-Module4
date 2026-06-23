package com.wanted.backend.domain.wishlist.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AddWishlistItemRequest(
        @Schema(description = "찜할 강의 ID", example = "1")
        @NotNull Long courseId
) {}
