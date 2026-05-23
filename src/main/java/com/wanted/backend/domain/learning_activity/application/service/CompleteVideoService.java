package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.CompleteVideoCommand;
import com.wanted.backend.domain.learning_activity.application.policy.VideoCompletionPolicy;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.usecase.CompleteVideoUseCase;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.domain.repository.VideoProgressRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CompleteVideoService implements CompleteVideoUseCase {

    private final VideoCatalogPort videoCatalogPort;
    private final VideoProgressRepository videoProgressRepository;
    private final VideoAccessService videoAccessService;
    private final VideoCompletionPolicy videoCompletionPolicy;

    @Override
    public void handle(CompleteVideoCommand command) {
        Long memberId = command.memberId();
        Long videoId = command.videoId();

        VideoAccessInfo accessInfo = videoCatalogPort.findByVideoId(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));

        videoAccessService.validatePlayable(memberId, accessInfo);

        VideoProgress progress = videoProgressRepository.findByMemberIdAndVideoId(memberId, videoId)
                .orElse(VideoProgress.empty(memberId, accessInfo.courseId(), videoId));

        if (!videoCompletionPolicy.canComplete(progress.watchTimeSec(), accessInfo.durationSeconds())) {
            throw new BusinessException(ErrorCode.VIDEO_COMPLETION_CONDITION_NOT_MET);
        }

        videoProgressRepository.save(progress.complete(LocalDateTime.now()));
    }
}
