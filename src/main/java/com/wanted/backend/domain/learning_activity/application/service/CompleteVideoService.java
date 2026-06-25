package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import com.wanted.backend.domain.learning_activity.application.policy.VideoCompletionPolicy;
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

    private static final LearningActivityAction ACTION = LearningActivityAction.COMPLETE_VIDEO;

    private final PlayableVideoProgressReader playableVideoProgressReader;
    private final VideoProgressRepository videoProgressRepository;
    private final VideoCompletionPolicy videoCompletionPolicy;
    private final LearningActivityMetricRecorder metricRecorder;

    @Override
    public void handle(MemberVideoCommand command) {
        String errorCode = "UNKNOWN";
        try {
            Long memberId = command.memberId();
            Long videoId = command.videoId();

            PlayableVideoProgressReader.PlayableVideoProgress playable =
                    playableVideoProgressReader.get(memberId, videoId);
            VideoAccessInfo accessInfo = playable.accessInfo();
            VideoProgress progress = playable.progress();

            if (!videoCompletionPolicy.canComplete(effectiveProgressSeconds(progress), accessInfo.durationSeconds())) {
                throw new BusinessException(ErrorCode.VIDEO_COMPLETION_CONDITION_NOT_MET);
            }

            videoProgressRepository.save(progress.complete(LocalDateTime.now()));
            errorCode = null;
        } catch (BusinessException e) {
            errorCode = e.getErrorCode().name();
            throw e;
        } finally {
            metricRecorder.recordResult(ACTION, errorCode);
        }
    }

    private int effectiveProgressSeconds(VideoProgress progress) {
        int watchTimeSec = progress.watchTimeSec() == null ? 0 : progress.watchTimeSec();
        int lastPositionSec = progress.lastPositionSec() == null ? 0 : progress.lastPositionSec();

        return Math.max(watchTimeSec, lastPositionSec);
    }
}
