package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.SaveVideoPositionCommand;
import com.wanted.backend.domain.learning_activity.application.usecase.SaveVideoPositionUseCase;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.domain.repository.VideoProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveVideoPositionService implements SaveVideoPositionUseCase {

    private final PlayableVideoProgressReader playableVideoProgressReader;
    private final VideoProgressRepository videoProgressRepository;

    @Override
    public void handle(SaveVideoPositionCommand command) {
        Long memberId = command.memberId();
        Long videoId = command.videoId();

        VideoProgress progress = playableVideoProgressReader.get(memberId, videoId).progress();

        videoProgressRepository.save(progress.updateLastPosition(command.positionSeconds()));
    }
}
