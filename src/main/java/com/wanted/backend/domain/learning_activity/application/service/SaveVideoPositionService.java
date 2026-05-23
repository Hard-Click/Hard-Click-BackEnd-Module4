package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.SaveVideoPositionCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.usecase.SaveVideoPositionUseCase;
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
@Transactional
public class SaveVideoPositionService implements SaveVideoPositionUseCase {

    private final VideoCatalogPort videoCatalogPort;
    private final VideoProgressRepository videoProgressRepository;
    private final VideoAccessService videoAccessService;

    @Override
    public void handle(SaveVideoPositionCommand command) {
        Long memberId = 1L; // TODO: 인증 구현 시 현재 로그인 사용자 ID로 교체.
        Long videoId = command.videoId();

        VideoAccessInfo accessInfo = videoCatalogPort.findByVideoId(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));

        videoAccessService.validatePlayable(memberId, accessInfo);

        VideoProgress progress = videoProgressRepository.findByMemberIdAndVideoId(memberId, videoId)
                .orElse(VideoProgress.empty(memberId, accessInfo.courseId(), videoId));

        videoProgressRepository.save(progress.updateLastPosition(command.positionSeconds()));
    }
}
