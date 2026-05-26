package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.presentation.api.request.AccountLockPasswordChangeRequest;
import com.wanted.backend.domain.identity.presentation.api.request.AccountLockVerifyRequest;

public interface AccountLockUseCase {
    String verify(AccountLockVerifyRequest request);
    void changePassword(AccountLockPasswordChangeRequest request);
}