package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.SaveVideoPositionCommand;

public interface SaveVideoPositionUseCase {

    void handle(SaveVideoPositionCommand command);
}
