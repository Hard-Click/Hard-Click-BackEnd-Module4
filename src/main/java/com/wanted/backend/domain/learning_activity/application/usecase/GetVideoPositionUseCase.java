package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import io.swagger.v3.oas.annotations.media.Schema;

public interface GetVideoPositionUseCase {

    VideoPositionView handle(MemberVideoCommand command);

    @Schema(description = "이어보기 위치 응답")
    record VideoPositionView(
            @Schema(description = "영상 ID (lessonId와 동일)", example = "10")
            Long videoId,

            @Schema(description = "마지막 재생 위치 (초)", example = "320")
            Integer positionSeconds
    ) {
    }
}
