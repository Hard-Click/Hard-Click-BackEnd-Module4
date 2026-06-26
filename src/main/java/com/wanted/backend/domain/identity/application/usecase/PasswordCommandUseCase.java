package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.AccountLockVerifyResult;
import com.wanted.backend.domain.identity.application.PasswordChangeResult;
import com.wanted.backend.domain.identity.application.command.AccountLockPasswordChangeCommand;
import com.wanted.backend.domain.identity.application.command.AccountLockVerifyCommand;
import com.wanted.backend.domain.identity.application.command.ResetPasswordCommand;
import com.wanted.backend.domain.identity.application.command.UpdatePasswordCommand;

public interface PasswordCommandUseCase {
    PasswordChangeResult updatePassword(Long memberId, UpdatePasswordCommand command);
    PasswordChangeResult resetPassword(ResetPasswordCommand command);
    AccountLockVerifyResult verify(AccountLockVerifyCommand command);
    PasswordChangeResult changePassword(AccountLockPasswordChangeCommand command);
    void verifyCurrentPassword(Long memberId, String currentPassword);
}
