package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.GetVideoPositionCommand;
import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.usecase.GetVideoPositionUseCase;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetVideoPositionService implements GetVideoPositionUseCase {

    private final PlayableVideoProgressReader playableVideoProgressReader;

    @Override
    public VideoPositionView handle(MemberVideoCommand command) {
        Long memberId = command.memberId();
        Long videoId = command.videoId();

        VideoProgress progress = playableVideoProgressReader.get(memberId, videoId).progress();

        return new VideoPositionView(videoId, progress.lastPositionSec());
    }
}
