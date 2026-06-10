package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.AccountLockPasswordChangeCommand;
import com.wanted.backend.domain.identity.application.command.AccountLockVerifyCommand;

public interface AccountLockUseCase {
    void sendCode(String email);

    String verify(AccountLockVerifyCommand command);

    void changePassword(AccountLockPasswordChangeCommand command);
}