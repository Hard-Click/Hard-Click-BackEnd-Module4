package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.UpdatePasswordCommand;

public interface UpdatePasswordUseCase {
    void updatePassword(Long memberId, UpdatePasswordCommand command);
}