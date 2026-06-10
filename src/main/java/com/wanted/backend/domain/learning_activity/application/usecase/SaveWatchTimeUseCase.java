package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.SaveWatchTimeCommand;

public interface SaveWatchTimeUseCase {

    void handle(SaveWatchTimeCommand command);
}
