package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.command.ChangeCourseStatusCommand;
import com.wanted.backend.domain.cource.application.command.CreateCourseCommand;
import com.wanted.backend.domain.cource.application.command.UpdateCourseCommand;
import com.wanted.backend.domain.cource.application.command.UploadCourseThumbnailCommand;
import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;
import com.wanted.backend.domain.cource.application.port.CourseVideoCatalogSyncPort;
import com.wanted.backend.domain.cource.application.port.ThumbnailStoragePort;
import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import com.wanted.backend.domain.cource.application.usecase.CourseCommandUseCase;
import com.wanted.backend.domain.cource.domain.dto.CourseAuthorInfo;
import com.wanted.backend.domain.cource.domain.event.CourseCreatedEvent;
import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.model.CourseSection;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.Lesson;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import com.wanted.backend.domain.cource.domain.repository.LessonRepository;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseCommandService implements CourseCommandUseCase {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final VideoStoragePort videoStoragePort;
    private final ThumbnailStoragePort thumbnailStoragePort;
    private final FileProcessingService fileProcessingService;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformTransactionManager transactionManager;
    private final Clock clock;
    private final NotificationRepository notificationRepository;
    private final CourseVideoCatalogSyncPort videoCatalogSyncPort;

    @Override
    public Long create(CreateCourseCommand command) {
        Instant now = clock.instant();

        List<CourseSection> sections = command.sections().stream()
                .map(s -> {
                    List<Lesson> lessons = s.lessons().stream()
                            .map(l -> Lesson.create(null, l.title(), l.description(), l.orderIndex(), l.durationSeconds(), now))
                            .toList();
                    return CourseSection.create(s.title(), s.orderIndex(), lessons);
                })
                .toList();

        Course course = Course.create(
                command.authorId(),
                command.title(),
                command.subject(),
                command.description(),
                command.thumbnailUrl(),
                command.priceType(),
                command.price(),
                sections,
                now,
                command.learningObjectives(),
                command.targetAudience(),
                command.techTags(),
                command.level()
        );

        Course saved = courseRepository.save(course);
        saved.pullDomainEvents().forEach(eventPublisher::publishEvent);

        eventPublisher.publishEvent(CourseCreatedEvent.of(
                saved.getId(), command.authorId(), saved.getTitle()));

        // 커밋 후 재생 스키마(course_curriculum/video)로 미러링
        registerVideoCatalogSync(saved.getId());

        return saved.getId();
    }

    @Override
    public void update(UpdateCourseCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        if (!course.getAuthorId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        List<CourseSection> newSections = command.sections().stream()
                .map(sc -> {
                    List<Lesson> lessons = sc.lessons().stream()
                            .map(lc -> lc.id() != null
                                    ? Lesson.restore(lc.id(), null, lc.title(), lc.description(),
                                                     lc.orderIndex(), null, null, null, null, null)
                                    : Lesson.create(null, lc.title(), lc.description(),
                                                    lc.orderIndex(), lc.durationSeconds(), clock.instant()))
                            .toList();
                    return sc.id() != null
                            ? CourseSection.restore(sc.id(), sc.title(), sc.orderIndex(), lessons)
                            : CourseSection.create(sc.title(), sc.orderIndex(), lessons);
                })
                .toList();

        course.update(command.title(), command.subject(), command.description(),
                command.thumbnailUrl(), command.priceType(), command.price(), newSections,
                command.learningObjectives(), command.targetAudience(),
                command.techTags(), command.level());

        courseRepository.save(course);

        // 섹션/레슨 변경을 재생 스키마(course_curriculum/video)에 반영
        registerVideoCatalogSync(command.courseId());
    }

    @Override
    public void delete(Long courseId, Long requesterId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.getAuthorId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        course.softDelete();
        courseRepository.save(course);
        notificationRepository.deleteByRedirectUrlStartingWith("/admin/courses/" + courseId);
    }

    @Override
    public void changeStatus(ChangeCourseStatusCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        if (!course.getAuthorId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        if (command.targetStatus() == CourseStatus.PUBLISHED) {
            course.publish();
        } else {
            course.unpublish();
        }

        courseRepository.save(course);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String uploadLessonVideo(UploadLessonVideoCommand command) {
        Lesson lesson = lessonRepository.findById(command.lessonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        // 삭제된 강의 차단 + 강의 작성자 본인만 영상 업로드 가능
        CourseAuthorInfo courseInfo = lessonRepository.findCourseAuthorInfo(command.lessonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        if (courseInfo.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!courseInfo.authorId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        // S3 업로드(네트워크 블로킹 호출)는 트랜잭션 밖에서 수행해 DB 커넥션을 점유하지 않는다.
        VideoStoragePort.StoredVideo storedVideo = videoStoragePort.store(
                command.lessonId(),
                command.originalFilename(),
                command.videoData()
        );

        try {
            persistUploadedVideo(lesson, storedVideo, command.lessonId(), courseInfo.courseId());
        } catch (RuntimeException e) {
            // DB 저장이 실패하면 이미 업로드된 S3 객체가 orphan으로 남지 않도록 보상 삭제한다.
            videoStoragePort.delete(storedVideo.key());
            throw e;
        }

        return storedVideo.presignedUrl();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String uploadCourseThumbnail(UploadCourseThumbnailCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!course.getAuthorId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        ThumbnailStoragePort.StoredThumbnail stored = thumbnailStoragePort.store(
                command.courseId(), command.originalFilename(), command.imageData());

        String oldKey = course.getThumbnailUrl();
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                course.updateThumbnail(stored.key());
                courseRepository.save(course);
            });
        } catch (RuntimeException e) {
            thumbnailStoragePort.delete(stored.key());
            throw e;
        }

        // 이전 썸네일 S3 객체 정리 (http(s) URL이면 외부 이미지이므로 삭제 안 함)
        if (oldKey != null && !oldKey.startsWith("http")) {
            thumbnailStoragePort.delete(oldKey);
        }

        return stored.presignedUrl();
    }

    // lesson 갱신 + 커밋 후 비동기 처리 트리거만 짧은 트랜잭션으로 묶는다.
    // (uploadLessonVideo 자체는 NOT_SUPPORTED라 트랜잭션이 없어, 별도로 새 트랜잭션을 열어야
    // afterCommit 동기화가 유효하다.)
    private void persistUploadedVideo(Lesson lesson, VideoStoragePort.StoredVideo storedVideo,
                                      Long lessonId, Long courseId) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            lesson.attachVideo(storedVideo.presignedUrl(), storedVideo.key());
            lessonRepository.save(lesson);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileProcessingService.process(lessonId);
                    // 업로드된 영상(s3_key)을 재생 스키마(video)로 반영
                    videoCatalogSyncPort.syncByCourse(courseId);
                }
            });
        });
    }

    // 커밋 이후 작성 스키마(course_section/lesson)를 재생 스키마(course_curriculum/video)로 미러링한다.
    private void registerVideoCatalogSync(Long courseId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                videoCatalogSyncPort.syncByCourse(courseId);
            }
        });
    }
}
