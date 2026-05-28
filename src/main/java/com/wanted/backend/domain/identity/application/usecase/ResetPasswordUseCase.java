package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.ResetPasswordCommand;

public interface ResetPasswordUseCase {
    void resetPassword(ResetPasswordCommand command);
}