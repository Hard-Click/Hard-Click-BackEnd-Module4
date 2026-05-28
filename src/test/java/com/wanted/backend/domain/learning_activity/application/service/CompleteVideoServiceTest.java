package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.CompleteVideoCommand;
import com.wanted.backend.domain.learning_activity.application.policy.VideoCompletionPolicy;
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

class CompleteVideoServiceTest {

    private VideoCatalogPort videoCatalogPort;
    private VideoProgressRepository videoProgressRepository;
    private VideoAccessService videoAccessService;
    private CompleteVideoService service;

    @BeforeEach
    void setUp() {
        videoCatalogPort = mock(VideoCatalogPort.class);
        videoProgressRepository = mock(VideoProgressRepository.class);
        videoAccessService = mock(VideoAccessService.class);
        service = new CompleteVideoService(
                videoCatalogPort,
                videoProgressRepository,
                videoAccessService,
                new VideoCompletionPolicy()
        );
    }

    @Test
    void 시청_시간이_충분하면_영상을_완료_처리한다() {
        VideoAccessInfo accessInfo = accessInfo();
        VideoProgress progress = new VideoProgress(100L, 1L, 20L, 10L, 42, 270, false, null);
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(1L, 10L)).thenReturn(Optional.of(progress));

        service.handle(new CompleteVideoCommand(1L, 10L));

        ArgumentCaptor<VideoProgress> captor = ArgumentCaptor.forClass(VideoProgress.class);
        verify(videoAccessService).validatePlayable(1L, accessInfo);
        verify(videoProgressRepository).save(captor.capture());
        assertThat(captor.getValue().completed()).isTrue();
        assertThat(captor.getValue().completedAt()).isNotNull();
        assertThat(captor.getValue().watchTimeSec()).isEqualTo(270);
    }

    @Test
    void 시청_시간이_부족하면_예외가_발생한다() {
        VideoAccessInfo accessInfo = accessInfo();
        VideoProgress progress = new VideoProgress(100L, 1L, 20L, 10L, 42, 269, false, null);
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(1L, 10L)).thenReturn(Optional.of(progress));

        assertThatThrownBy(() -> service.handle(new CompleteVideoCommand(1L, 10L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VIDEO_COMPLETION_CONDITION_NOT_MET);
    }

    @Test
    void 영상_접근_정보가_없으면_예외가_발생한다() {
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new CompleteVideoCommand(1L, 10L)))
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
