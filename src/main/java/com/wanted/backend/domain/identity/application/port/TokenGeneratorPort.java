package com.wanted.backend.domain.identity.application.port;

import com.wanted.backend.domain.identity.domain.model.AuthToken;

public interface TokenGeneratorPort {
    AuthToken generateToken(Long memberId, String role);
}
