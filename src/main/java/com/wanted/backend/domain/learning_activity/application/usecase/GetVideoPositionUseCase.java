package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.GetVideoPositionCommand;

public interface GetVideoPositionUseCase {

    VideoPositionView handle(GetVideoPositionCommand command);

    record VideoPositionView(
            Long videoId,
            Integer positionSeconds
    ) {
    }
}
