package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.GetVideoProgressCommand;
import com.wanted.backend.domain.learning_activity.application.usecase.GetVideoProgressUseCase;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetVideoProgressService implements GetVideoProgressUseCase {

    private static final BigDecimal MAX_PROGRESS_RATE = BigDecimal.valueOf(100);

    private final PlayableVideoProgressReader playableVideoProgressReader;

    @Override
    public VideoProgressView handle(GetVideoProgressCommand command) {
        Long memberId = command.memberId();
        Long videoId = command.videoId();

        PlayableVideoProgressReader.PlayableVideoProgress playable =
                playableVideoProgressReader.get(memberId, videoId);
        VideoAccessInfo accessInfo = playable.accessInfo();
        VideoProgress progress = playable.progress();

        return new VideoProgressView(
                videoId,
                progress.lastPositionSec(),
                progress.watchTimeSec(),
                accessInfo.durationSeconds(),
                calculateProgressRate(progress.watchTimeSec(), accessInfo.durationSeconds()),
                progress.completed(),
                progress.completedAt()
        );
    }

    private BigDecimal calculateProgressRate(Integer watchTimeSeconds, Integer durationSeconds) {
        if (watchTimeSeconds == null || durationSeconds == null || durationSeconds <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        BigDecimal progressRate = BigDecimal.valueOf(watchTimeSeconds)
                .multiply(MAX_PROGRESS_RATE)
                .divide(BigDecimal.valueOf(durationSeconds), 1, RoundingMode.HALF_UP);

        return progressRate.min(MAX_PROGRESS_RATE).setScale(1, RoundingMode.HALF_UP);
    }
}
