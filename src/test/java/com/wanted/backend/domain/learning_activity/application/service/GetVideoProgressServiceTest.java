package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.GetVideoProgressCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.usecase.GetVideoProgressUseCase.VideoProgressView;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.domain.repository.VideoProgressRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetVideoProgressServiceTest {

    private VideoCatalogPort videoCatalogPort;
    private VideoProgressRepository videoProgressRepository;
    private VideoAccessService videoAccessService;
    private GetVideoProgressService service;

    @BeforeEach
    void setUp() {
        videoCatalogPort = mock(VideoCatalogPort.class);
        videoProgressRepository = mock(VideoProgressRepository.class);
        videoAccessService = mock(VideoAccessService.class);
        service = new GetVideoProgressService(videoCatalogPort, videoProgressRepository, videoAccessService);
    }

    @Test
    void 진도_정보가_있으면_영상_진도를_반환한다() {
        VideoAccessInfo accessInfo = accessInfo();
        LocalDateTime completedAt = LocalDateTime.of(2026, 5, 23, 10, 30);
        VideoProgress progress = new VideoProgress(100L, 1L, 20L, 10L, 142, 270, true, completedAt);
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(1L, 10L)).thenReturn(Optional.of(progress));

        VideoProgressView result = service.handle(new GetVideoProgressCommand(1L, 10L));

        verify(videoAccessService).validatePlayable(1L, accessInfo);
        assertThat(result.videoId()).isEqualTo(10L);
        assertThat(result.lastPositionSeconds()).isEqualTo(142);
        assertThat(result.watchTimeSeconds()).isEqualTo(270);
        assertThat(result.completed()).isTrue();
        assertThat(result.completedAt()).isEqualTo(completedAt);
    }

    @Test
    void 진도_정보가_없으면_기본_진도를_반환한다() {
        VideoAccessInfo accessInfo = accessInfo();
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(1L, 10L)).thenReturn(Optional.empty());

        VideoProgressView result = service.handle(new GetVideoProgressCommand(1L, 10L));

        verify(videoAccessService).validatePlayable(1L, accessInfo);
        assertThat(result.videoId()).isEqualTo(10L);
        assertThat(result.lastPositionSeconds()).isZero();
        assertThat(result.watchTimeSeconds()).isZero();
        assertThat(result.completed()).isFalse();
        assertThat(result.completedAt()).isNull();
    }

    @Test
    void 영상_접근_정보가_없으면_예외가_발생한다() {
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new GetVideoProgressCommand(1L, 10L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VIDEO_NOT_FOUND);
    }

    private VideoAccessInfo accessInfo() {
        return new VideoAccessInfo(
                10L,
                20L,
                "PUBLISHED",
                10000,
                false,
                "https://stream.example.com/video.m3u8",
                300
        );
    }
}
