package com.wanted.backend.domain.identity.presentation.api.response;

public record DuplicateCheckResponse(
        boolean exists
) {
}