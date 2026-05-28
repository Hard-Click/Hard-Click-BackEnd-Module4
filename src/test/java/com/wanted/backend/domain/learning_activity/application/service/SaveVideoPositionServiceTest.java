package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.SaveVideoPositionCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.domain.repository.VideoProgressRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaveVideoPositionServiceTest {

    private VideoCatalogPort videoCatalogPort;
    private VideoProgressRepository videoProgressRepository;
    private VideoAccessService videoAccessService;
    private SaveVideoPositionService service;

    @BeforeEach
    void setUp() {
        videoCatalogPort = mock(VideoCatalogPort.class);
        videoProgressRepository = mock(VideoProgressRepository.class);
        videoAccessService = mock(VideoAccessService.class);
        service = new SaveVideoPositionService(videoCatalogPort, videoProgressRepository, videoAccessService);
    }

    @Test
    void updatesLastPositionWhenProgressExists() {
        VideoAccessInfo accessInfo = accessInfo();
        VideoProgress progress = new VideoProgress(100L, 1L, 20L, 10L, 42, 120, false, null);
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(1L, 10L)).thenReturn(Optional.of(progress));

        service.handle(new SaveVideoPositionCommand(1L, 10L, 142));

        ArgumentCaptor<VideoProgress> captor = ArgumentCaptor.forClass(VideoProgress.class);
        verify(videoAccessService).validatePlayable(1L, accessInfo);
        verify(videoProgressRepository).save(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(100L);
        assertThat(captor.getValue().lastPositionSec()).isEqualTo(142);
        assertThat(captor.getValue().watchTimeSec()).isEqualTo(120);
    }

    @Test
    void createsProgressWhenProgressDoesNotExist() {
        VideoAccessInfo accessInfo = accessInfo();
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(1L, 10L)).thenReturn(Optional.empty());

        service.handle(new SaveVideoPositionCommand(1L, 10L, 142));

        ArgumentCaptor<VideoProgress> captor = ArgumentCaptor.forClass(VideoProgress.class);
        verify(videoProgressRepository).save(captor.capture());
        assertThat(captor.getValue().id()).isNull();
        assertThat(captor.getValue().memberId()).isEqualTo(1L);
        assertThat(captor.getValue().courseId()).isEqualTo(20L);
        assertThat(captor.getValue().videoId()).isEqualTo(10L);
        assertThat(captor.getValue().lastPositionSec()).isEqualTo(142);
        assertThat(captor.getValue().watchTimeSec()).isZero();
        assertThat(captor.getValue().completed()).isFalse();
    }

    @Test
    void throwsVideoNotFoundWhenVideoAccessInfoDoesNotExist() {
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new SaveVideoPositionCommand(1L, 10L, 142)))
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
