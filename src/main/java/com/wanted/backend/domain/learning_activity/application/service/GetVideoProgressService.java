package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.usecase.GetVideoProgressUseCase;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.domain.repository.VideoProgressRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
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

    private final VideoCatalogPort videoCatalogPort;
    private final VideoProgressRepository videoProgressRepository;
    private final VideoAccessService videoAccessService;

    @Override
    public VideoProgressView handle(MemberVideoCommand command) {
        Long memberId = command.memberId();
        Long videoId = command.videoId();

        VideoAccessInfo accessInfo = videoCatalogPort.findByVideoId(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));

        videoAccessService.validatePlayable(memberId, accessInfo);

        VideoProgress progress = videoProgressRepository.findByMemberIdAndVideoId(memberId, videoId)
                .orElse(VideoProgress.empty(memberId, accessInfo.courseId(), videoId));

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
