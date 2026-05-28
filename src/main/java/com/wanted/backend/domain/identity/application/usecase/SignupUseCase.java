package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.SignupCommand;

public interface SignupUseCase {

    Long signup(SignupCommand command);
}