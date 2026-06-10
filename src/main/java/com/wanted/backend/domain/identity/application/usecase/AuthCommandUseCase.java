package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.domain.model.AuthToken;

public interface AuthCommandUseCase {
    AuthToken login(String username, String rawPassword);

    AuthToken refresh(String refreshToken);
    void logout(String refreshToken);
}

