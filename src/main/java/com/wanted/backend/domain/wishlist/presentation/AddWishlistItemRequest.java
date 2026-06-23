package com.wanted.backend.domain.wishlist.presentation;

import jakarta.validation.constraints.NotNull;

public record AddWishlistItemRequest(
        @NotNull Long courseId
) {}
