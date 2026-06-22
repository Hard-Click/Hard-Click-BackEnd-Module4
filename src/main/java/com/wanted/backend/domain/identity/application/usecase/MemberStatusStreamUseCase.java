package com.wanted.backend.domain.identity.application.usecase;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface MemberStatusStreamUseCase {
    SseEmitter connect(Long memberId);
}
