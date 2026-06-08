package com.wanted.backend.domain.identity.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "빈 응답 객체")
public record EmptyResponse() {
}