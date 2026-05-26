package com.wanted.backend.domain.identity.presentation.api.response;


public record EmailVerifyResponse(
        String emailVerificationToken
) {
}