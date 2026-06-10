package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;

public interface CompleteVideoUseCase {

    void handle(MemberVideoCommand command);
}
