package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.usecase.VideoPlayUseCase;
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

class VideoPlayServiceTest {

    private VideoCatalogPort videoCatalogPort;
    private VideoProgressRepository videoProgressRepository;
    private VideoAccessService videoAccessService;
    private VideoPlayService service;

    @BeforeEach
    void setUp() {
        videoCatalogPort = mock(VideoCatalogPort.class);
        videoProgressRepository = mock(VideoProgressRepository.class);
        videoAccessService = mock(VideoAccessService.class);
        service = new VideoPlayService(videoCatalogPort, videoProgressRepository, videoAccessService);
    }

    @Test
    void 기존_진도_정보가_있으면_영상_재생_정보와_함께_반환한다() {
        VideoAccessInfo accessInfo = accessInfo();
        VideoProgress progress = new VideoProgress(
                100L,
                1L,
                20L,
                10L,
                42,
                120,
                true,
                LocalDateTime.now()
        );
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(2L, 10L)).thenReturn(Optional.of(progress));

        VideoPlayUseCase.VideoPlayView result = service.handle(new MemberVideoCommand(2L, 10L));

        assertThat(result.videoId()).isEqualTo(10L);
        assertThat(result.courseId()).isEqualTo(20L);
        assertThat(result.streamingUrl()).isEqualTo("https://stream.example.com/video.m3u8");
        assertThat(result.durationSeconds()).isEqualTo(300);
        assertThat(result.lastPositionSec()).isEqualTo(42);
        assertThat(result.watchTimeSec()).isEqualTo(120);
        assertThat(result.completed()).isTrue();
        verify(videoAccessService).validatePlayable(2L, accessInfo);
    }

    @Test
    void 진도_정보가_없으면_기본_재생_위치를_반환한다() {
        VideoAccessInfo accessInfo = accessInfo();
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(2L, 10L)).thenReturn(Optional.empty());

        VideoPlayUseCase.VideoPlayView result = service.handle(new MemberVideoCommand(2L, 10L));

        assertThat(result.lastPositionSec()).isZero();
        assertThat(result.watchTimeSec()).isZero();
        assertThat(result.completed()).isFalse();
    }

    @Test
    void 영상_접근_정보가_없으면_예외가_발생한다() {
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new MemberVideoCommand(2L, 10L)))
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
