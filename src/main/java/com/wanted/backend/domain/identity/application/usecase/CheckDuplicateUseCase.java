package com.wanted.backend.domain.identity.application.usecase;

public interface CheckDuplicateUseCase {
    boolean isUsernameDuplicated(String username);
    boolean isEmailDuplicated(String email);
}
