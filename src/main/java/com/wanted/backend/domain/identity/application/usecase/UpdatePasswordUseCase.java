package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.presentation.api.request.UpdatePasswordRequest;

public interface UpdatePasswordUseCase {
    void updatePassword(Long memberId, UpdatePasswordRequest request);
}