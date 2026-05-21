package com.wanted.backend.global.domain;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
