package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.WithdrawMemberCommand;

public interface WithdrawMemberUseCase {
    void withdraw(Long memberId, WithdrawMemberCommand command);
}