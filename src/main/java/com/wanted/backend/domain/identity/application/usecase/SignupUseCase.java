package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.presentation.api.request.SignupRequest;

public interface SignupUseCase {

    Long signup(SignupRequest request);
}