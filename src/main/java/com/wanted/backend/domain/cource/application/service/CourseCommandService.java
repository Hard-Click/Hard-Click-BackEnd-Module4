package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.command.ChangeCourseStatusCommand;
import com.wanted.backend.domain.cource.application.command.ConfirmVideoUploadCommand;
import com.wanted.backend.domain.cource.application.command.CreateCourseCommand;
import com.wanted.backend.domain.cource.application.command.RequestVideoUploadCommand;
import com.wanted.backend.domain.cource.application.command.UpdateCourseCommand;
import com.wanted.backend.domain.cource.application.command.UploadCourseThumbnailCommand;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseCommandService implements CourseCommandUseCase {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final VideoStoragePort videoStoragePort;
    private final ThumbnailStoragePort thumbnailStoragePort;
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
    public VideoStoragePort.PresignedUpload requestVideoUpload(RequestVideoUploadCommand command) {
        CourseAuthorInfo courseInfo = lessonRepository.findCourseAuthorInfo(command.lessonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        if (courseInfo.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!courseInfo.authorId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        return videoStoragePort.generatePresignedPutUrl(command.lessonId(), command.originalFilename());
    }

    @Override
    public void confirmVideoUpload(ConfirmVideoUploadCommand command) {
        Lesson lesson = lessonRepository.findById(command.lessonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        CourseAuthorInfo courseInfo = lessonRepository.findCourseAuthorInfo(command.lessonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        if (courseInfo.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!courseInfo.authorId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        // video_url에 s3Key 저장 후 COMPLETED로 전이 (presigned URL 직접 업로드는 별도 처리 단계 없음)
        lesson.attachVideo(command.s3Key(), command.s3Key());
        lesson.completeProcessing();
        lessonRepository.save(lesson);

        registerVideoCatalogSync(courseInfo.courseId());
    }

    @Override
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

        if (oldKey != null && !oldKey.startsWith("http")) {
            thumbnailStoragePort.delete(oldKey);
        }

        return stored.presignedUrl();
    }

    private void registerVideoCatalogSync(Long courseId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                            videoCatalogSyncPort.syncByCourse(courseId));
                } catch (Exception e) {
                    log.warn("video catalog 미러링 실패(courseId={}) — 강의 등록은 정상 처리됨", courseId, e);
                }
            }
        });
    }
}
