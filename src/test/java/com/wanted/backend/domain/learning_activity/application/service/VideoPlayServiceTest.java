package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.port.VideoPlaybackUrlPort;
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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoPlayServiceTest {

    private VideoCatalogPort videoCatalogPort;
    private VideoProgressRepository videoProgressRepository;
    private VideoAccessService videoAccessService;
    private VideoPlaybackUrlPort videoPlaybackUrlPort;
    private VideoPlayService service;

    @BeforeEach
    void setUp() {
        videoCatalogPort = mock(VideoCatalogPort.class);
        videoProgressRepository = mock(VideoProgressRepository.class);
        videoAccessService = mock(VideoAccessService.class);
        videoPlaybackUrlPort = mock(VideoPlaybackUrlPort.class);
        PlayableVideoProgressReader playableVideoProgressReader =
                new PlayableVideoProgressReader(videoCatalogPort, videoProgressRepository, videoAccessService);
        service = new VideoPlayService(playableVideoProgressReader, videoPlaybackUrlPort);
    }

    @Test
    void 기존_진도_정보가_있으면_권한_검증_후_재생_url과_함께_반환한다() {
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
        when(videoPlaybackUrlPort.generatePlaybackUrl(accessInfo))
                .thenReturn("https://signed.example.com/videos/10.mp4");

        VideoPlayUseCase.VideoPlayView result = service.handle(new MemberVideoCommand(2L, 10L));

        assertThat(result.videoId()).isEqualTo(10L);
        assertThat(result.courseId()).isEqualTo(20L);
        assertThat(result.streamingUrl()).isEqualTo("https://signed.example.com/videos/10.mp4");
        assertThat(result.durationSeconds()).isEqualTo(300);
        assertThat(result.lastPositionSec()).isEqualTo(42);
        assertThat(result.watchTimeSec()).isEqualTo(120);
        assertThat(result.completed()).isTrue();
        var inOrder = inOrder(videoAccessService, videoPlaybackUrlPort);
        inOrder.verify(videoAccessService).validatePlayable(2L, accessInfo);
        inOrder.verify(videoPlaybackUrlPort).generatePlaybackUrl(accessInfo);
    }

    @Test
    void 진도_정보가_없으면_기본_재생_위치를_반환한다() {
        VideoAccessInfo accessInfo = accessInfo();
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(2L, 10L)).thenReturn(Optional.empty());
        when(videoPlaybackUrlPort.generatePlaybackUrl(accessInfo))
                .thenReturn("https://signed.example.com/videos/10.mp4");

        VideoPlayUseCase.VideoPlayView result = service.handle(new MemberVideoCommand(2L, 10L));

        assertThat(result.streamingUrl()).isEqualTo("https://signed.example.com/videos/10.mp4");
        assertThat(result.lastPositionSec()).isZero();
        assertThat(result.watchTimeSec()).isZero();
        assertThat(result.completed()).isFalse();
    }

    @Test
    void playback_url을_생성할_수_없으면_예외를_전파한다() {
        VideoAccessInfo accessInfo = accessInfoWithoutPlaybackUrl();
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(2L, 10L)).thenReturn(Optional.empty());
        when(videoPlaybackUrlPort.generatePlaybackUrl(accessInfo))
                .thenThrow(new BusinessException(ErrorCode.VIDEO_PLAYBACK_URL_NOT_FOUND));

        assertThatThrownBy(() -> service.handle(new MemberVideoCommand(2L, 10L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VIDEO_PLAYBACK_URL_NOT_FOUND);
    }

    @Test
    void 영상_접근_정보가_없으면_예외가_발생한다() {
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new MemberVideoCommand(2L, 10L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VIDEO_NOT_FOUND);
        verify(videoPlaybackUrlPort, never()).generatePlaybackUrl(org.mockito.ArgumentMatchers.any());
    }

    private VideoAccessInfo accessInfo() {
        return new VideoAccessInfo(
                10L,
                20L,
                "PUBLISHED",
                10000,
                false,
                "videos/10.mp4",
                "https://stream.example.com/video.m3u8",
                300
        );
    }

    private VideoAccessInfo accessInfoWithoutPlaybackUrl() {
        return new VideoAccessInfo(
                10L,
                20L,
                "PUBLISHED",
                10000,
                false,
                null,
                null,
                300
        );
    }
}
