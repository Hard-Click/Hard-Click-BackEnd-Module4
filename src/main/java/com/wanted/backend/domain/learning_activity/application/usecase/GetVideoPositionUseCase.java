package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;

public interface GetVideoPositionUseCase {

    VideoPositionView handle(MemberVideoCommand command);

    record VideoPositionView(
            Long videoId,
            Integer positionSeconds
    ) {
    }
}
