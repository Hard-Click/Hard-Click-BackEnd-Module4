package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.VideoPlayCommand;
import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.usecase.VideoPlayUseCase;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoPlayService implements VideoPlayUseCase {

    private final PlayableVideoProgressReader playableVideoProgressReader;

    @Override
    public VideoPlayView handle(MemberVideoCommand command) {
        Long memberId = command.memberId();
        Long videoId = command.videoId();

        PlayableVideoProgressReader.PlayableVideoProgress playable =
                playableVideoProgressReader.get(memberId, videoId);
        VideoAccessInfo accessInfo = playable.accessInfo();
        VideoProgress progress = playable.progress();

        return new VideoPlayView(
                accessInfo.videoId(),
                accessInfo.courseId(),
                accessInfo.streamingUrl(),
                accessInfo.durationSeconds(),
                progress.lastPositionSec(),
                progress.watchTimeSec(),
                progress.completed()
        );
    }
}
