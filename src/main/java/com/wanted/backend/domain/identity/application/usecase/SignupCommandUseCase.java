package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.SignupCommand;

public interface SignupCommandUseCase {
    Long signup(SignupCommand command);
    boolean isUsernameDuplicated(String username);
    boolean isEmailDuplicated(String email);
}
