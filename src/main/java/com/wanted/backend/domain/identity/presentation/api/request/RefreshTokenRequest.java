package com.wanted.backend.domain.identity.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh Token이 필요합니다")
    private String refreshToken;
}