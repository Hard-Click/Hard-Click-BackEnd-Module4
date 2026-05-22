package com.wanted.backend.domain.enrollment_management.application.usecase;

import com.wanted.backend.domain.enrollment_management.application.command.EnrollCommand;

public interface EnrollUseCase {
    Long handle(EnrollCommand command);
}
