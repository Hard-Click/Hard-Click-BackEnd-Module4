package com.wanted.backend.domain.learning_activity.application.service;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.usecase.GetVideoPositionUseCase.VideoPositionView;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.domain.repository.VideoProgressRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetVideoPositionServiceTest {

    private VideoCatalogPort videoCatalogPort;
    private VideoProgressRepository videoProgressRepository;
    private VideoAccessService videoAccessService;
    private GetVideoPositionService service;

    @BeforeEach
    void setUp() {
        videoCatalogPort = mock(VideoCatalogPort.class);
        videoProgressRepository = mock(VideoProgressRepository.class);
        videoAccessService = mock(VideoAccessService.class);
        service = new GetVideoPositionService(videoCatalogPort, videoProgressRepository, videoAccessService);
    }

    @Test
    void 진도_정보가_있으면_마지막_재생_위치를_반환한다() {
        VideoAccessInfo accessInfo = accessInfo();
        VideoProgress progress = new VideoProgress(100L, 1L, 20L, 10L, 142, 200, false, null);
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(1L, 10L)).thenReturn(Optional.of(progress));

        VideoPositionView result = service.handle(new MemberVideoCommand(1L, 10L));

        verify(videoAccessService).validatePlayable(1L, accessInfo);
        assertThat(result.videoId()).isEqualTo(10L);
        assertThat(result.positionSeconds()).isEqualTo(142);
    }

    @Test
    void 진도_정보가_없으면_0을_반환한다() {
        VideoAccessInfo accessInfo = accessInfo();
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.of(accessInfo));
        when(videoProgressRepository.findByMemberIdAndVideoId(1L, 10L)).thenReturn(Optional.empty());

        VideoPositionView result = service.handle(new MemberVideoCommand(1L, 10L));

        assertThat(result.videoId()).isEqualTo(10L);
        assertThat(result.positionSeconds()).isZero();
    }

    @Test
    void 영상_접근_정보가_없으면_예외가_발생한다() {
        when(videoCatalogPort.findByVideoId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new MemberVideoCommand(1L, 10L)))
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
