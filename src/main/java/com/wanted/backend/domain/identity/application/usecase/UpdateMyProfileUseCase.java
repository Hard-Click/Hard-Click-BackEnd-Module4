package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.UpdateMyProfileCommand;

public interface UpdateMyProfileUseCase {

    MyProfileUpdateView handle(UpdateMyProfileCommand command);

    record MyProfileUpdateView(
            Long memberId,
            String name,
            String email,
            String profileImageUrl
    ) {
    }
}
