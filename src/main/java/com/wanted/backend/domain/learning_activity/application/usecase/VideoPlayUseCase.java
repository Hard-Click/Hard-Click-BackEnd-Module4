package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.VideoPlayCommand;

// 외부에 제공하는 계약
// 컨트롤러는 구현체인 VideoPlayService 몰라도 됨 유스케이스만 바라본다
public interface VideoPlayUseCase {

    // 해당 API 가 반환할 응답 모델
    VideoPlayView handle(VideoPlayCommand command);

    record VideoPlayView(
            Long videoId,
            Long courseId,
            String streamingUrl,
            Integer durationSeconds,
            Integer lastPositionSec,
            Integer watchTimeSec,
            Boolean completed
    ) {
    }
}
