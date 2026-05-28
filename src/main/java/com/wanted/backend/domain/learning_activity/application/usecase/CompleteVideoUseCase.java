package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.CompleteVideoCommand;

public interface CompleteVideoUseCase {

    void handle(CompleteVideoCommand command);
}
