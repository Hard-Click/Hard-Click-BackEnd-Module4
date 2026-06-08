package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.AccountLockPasswordChangeCommand;
import com.wanted.backend.domain.identity.application.command.AccountLockVerifyCommand;
import com.wanted.backend.domain.identity.application.command.ResetPasswordCommand;
import com.wanted.backend.domain.identity.application.command.UpdatePasswordCommand;

public interface PasswordCommandUseCase {
    void updatePassword(Long memberId, UpdatePasswordCommand command);
    void resetPassword(ResetPasswordCommand command);
    String verify(AccountLockVerifyCommand command);
    void changePassword(AccountLockPasswordChangeCommand command);
}
