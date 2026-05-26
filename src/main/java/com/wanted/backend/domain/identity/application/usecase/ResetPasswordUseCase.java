package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.presentation.api.request.ResetPasswordRequest;

public interface ResetPasswordUseCase {
    void resetPassword(ResetPasswordRequest request);
}