package com.wanted.backend.domain.identity.application.usecase;

public interface LogoutUseCase {
    void logout(String refreshToken);
}
