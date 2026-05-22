package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.VideoPlayCommand;
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

    /* comment.
    *   유스케이스 실행 순서 조립
    *   1. videoId로 재생 접근 정보 조회
    *   2. 없으면 VIDEO_NOT_FOUND
    *   3. 재생 권한 검증
    *   4. VideoProgress 조회
    *   5. 없으면 기본값 생성
    *   6. VideoPlayView 로 변환해서 반환
    *   즉 서비스는 port 와 repository 같은 계약만 알고 있다.
    * */

    private final VideoCatalogPort videoAccessPort;
    private final VideoProgressRepository videoProgressRepository;
    private final VideoAccessService videoAccessService;

    @Override
    public VideoPlayView handle(VideoPlayCommand command) {
        Long memberId = 1L; // TODO: 하드 코딩 해둠 / 인가 구현시 변경 예정.
        // 1번
        Long videoId = command.videoId();

        // 2번
        VideoAccessInfo accessInfo = videoAccessPort.findByVideoId(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));

        // 3번
        videoAccessService.validatePlayable(memberId, accessInfo);

        // 4번
        VideoProgress progress = videoProgressRepository.findByMemberIdAndVideoId(memberId, videoId)
                // 5번
                .orElse(VideoProgress.empty(memberId, accessInfo.courseId(), videoId));

        // 6번
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
