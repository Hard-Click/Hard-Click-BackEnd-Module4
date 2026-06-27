package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;
import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import com.wanted.backend.domain.cource.domain.dto.CourseAuthorInfo;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.Lesson;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import com.wanted.backend.domain.cource.domain.repository.LessonRepository;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseCommandServiceTest {

    private LessonRepository lessonRepository;
    private VideoStoragePort videoStoragePort;
    private CourseCommandService service;

    @BeforeEach
    void setUp() {
        CourseRepository courseRepository = mock(CourseRepository.class);
        lessonRepository = mock(LessonRepository.class);
        videoStoragePort = mock(VideoStoragePort.class);
        FileProcessingService fileProcessingService = mock(FileProcessingService.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

        service = new CourseCommandService(
                courseRepository,
                lessonRepository,
                videoStoragePort,
                fileProcessingService,
                eventPublisher,
                clock,
                notificationRepository
        );
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void 영상_업로드_시_presignedUrl과_s3Key가_레슨에_같이_저장된다() {
        Long lessonId = 10L;
        Long authorId = 1L;
        Lesson lesson = Lesson.create(5L, "1강", "설명", 0, null, Instant.now());
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonRepository.findCourseAuthorInfo(lessonId))
                .thenReturn(Optional.of(new CourseAuthorInfo(authorId, CourseStatus.PUBLISHED)));
        when(videoStoragePort.store(lessonId, "lecture.mp4", new byte[]{1, 2, 3}))
                .thenReturn(new VideoStoragePort.StoredVideo("videos/10_uuid.mp4", "https://s3.example.com/presigned"));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = service.uploadLessonVideo(new UploadLessonVideoCommand(lessonId, authorId, "lecture.mp4", new byte[]{1, 2, 3}));

        assertThat(result).isEqualTo("https://s3.example.com/presigned");
        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());
        assertThat(captor.getValue().getVideoUrl()).isEqualTo("https://s3.example.com/presigned");
        assertThat(captor.getValue().getS3Key()).isEqualTo("videos/10_uuid.mp4");
    }
}
