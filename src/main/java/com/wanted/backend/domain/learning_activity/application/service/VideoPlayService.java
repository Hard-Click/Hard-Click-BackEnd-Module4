package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.usecase.VideoPlayUseCase;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.domain.repository.VideoProgressRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoPlayService implements VideoPlayUseCase {

    private final VideoCatalogPort videoAccessPort;
    private final VideoProgressRepository videoProgressRepository;
    private final VideoAccessService videoAccessService;

    @Override
    public VideoPlayView handle(MemberVideoCommand command) {
        Long memberId = command.memberId();
        Long videoId = command.videoId();

        VideoAccessInfo accessInfo = videoAccessPort.findByVideoId(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));

        videoAccessService.validatePlayable(memberId, accessInfo);

        VideoProgress progress = videoProgressRepository.findByMemberIdAndVideoId(memberId, videoId)
                .orElse(VideoProgress.empty(memberId, accessInfo.courseId(), videoId));

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
