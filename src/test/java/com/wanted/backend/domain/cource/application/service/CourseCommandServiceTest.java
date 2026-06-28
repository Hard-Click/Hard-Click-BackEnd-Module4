package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;
import com.wanted.backend.domain.cource.application.port.CourseVideoCatalogSyncPort;
import com.wanted.backend.domain.cource.application.port.ThumbnailStoragePort;
import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import com.wanted.backend.domain.cource.domain.dto.CourseAuthorInfo;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.FileProcessingStatus;
import com.wanted.backend.domain.cource.domain.model.Lesson;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import com.wanted.backend.domain.cource.domain.repository.LessonRepository;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        ThumbnailStoragePort thumbnailStoragePort = mock(ThumbnailStoragePort.class);
        FileProcessingService fileProcessingService = mock(FileProcessingService.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        CourseVideoCatalogSyncPort videoCatalogSyncPort = mock(CourseVideoCatalogSyncPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

        service = new CourseCommandService(
                courseRepository,
                lessonRepository,
                videoStoragePort,
                thumbnailStoragePort,
                fileProcessingService,
                eventPublisher,
                // 실제 DB 없이도 TransactionTemplate의 트랜잭션 동기화(afterCommit)가 동작하게 해주는
                // 리소스 없는 트랜잭션 매니저(테스트 더블) — 단위테스트 표준 패턴.
                new ResourcelessTransactionManager(),
                clock,
                notificationRepository,
                videoCatalogSyncPort
        );
    }

    // 실제 DB 리소스 없이 트랜잭션 동기화(afterCommit 등) 생명주기만 정상 동작시키는 테스트 더블.
    private static class ResourcelessTransactionManager extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
        }
    }

    @Test
    void 영상_업로드_시_presignedUrl과_s3Key가_레슨에_같이_저장된다() {
        Long lessonId = 10L;
        Long authorId = 1L;
        Lesson lesson = Lesson.create(5L, "1강", "설명", 0, null, Instant.now());
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonRepository.findCourseAuthorInfo(lessonId))
                .thenReturn(Optional.of(new CourseAuthorInfo(100L, authorId, CourseStatus.PUBLISHED)));
        when(videoStoragePort.store(lessonId, "lecture.mp4", new byte[]{1, 2, 3}))
                .thenReturn(new VideoStoragePort.StoredVideo("videos/10_uuid.mp4", "https://s3.example.com/presigned"));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = service.uploadLessonVideo(new UploadLessonVideoCommand(lessonId, authorId, "lecture.mp4", new byte[]{1, 2, 3}));

        assertThat(result).isEqualTo("https://s3.example.com/presigned");
        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());
        assertThat(captor.getValue().getVideoUrl()).isEqualTo("https://s3.example.com/presigned");
        assertThat(captor.getValue().getS3Key()).isEqualTo("videos/10_uuid.mp4");
        assertThat(captor.getValue().getFileProcessingStatus()).isEqualTo(FileProcessingStatus.PENDING);
        verify(videoStoragePort, never()).delete(any());
    }

    @Test
    void db_저장이_실패하면_업로드된_S3_객체를_보상_삭제한다() {
        Long lessonId = 10L;
        Long authorId = 1L;
        Lesson lesson = Lesson.create(5L, "1강", "설명", 0, null, Instant.now());
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonRepository.findCourseAuthorInfo(lessonId))
                .thenReturn(Optional.of(new CourseAuthorInfo(100L, authorId, CourseStatus.PUBLISHED)));
        when(videoStoragePort.store(lessonId, "lecture.mp4", new byte[]{1, 2, 3}))
                .thenReturn(new VideoStoragePort.StoredVideo("videos/10_uuid.mp4", "https://s3.example.com/presigned"));
        when(lessonRepository.save(any(Lesson.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.uploadLessonVideo(
                new UploadLessonVideoCommand(lessonId, authorId, "lecture.mp4", new byte[]{1, 2, 3})))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");

        verify(videoStoragePort).delete("videos/10_uuid.mp4");
    }
}
