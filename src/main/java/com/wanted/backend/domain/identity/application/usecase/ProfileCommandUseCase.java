package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.UpdateMyProfileCommand;
import com.wanted.backend.domain.identity.application.command.WithdrawMemberCommand;

public interface ProfileCommandUseCase {

    MyProfileUpdateView handle(UpdateMyProfileCommand command);
    void withdraw(Long memberId, WithdrawMemberCommand command);

    record MyProfileUpdateView(
            Long memberId,
            String name,
            String email,
            String profileImageUrl
    ) {
    }
}
