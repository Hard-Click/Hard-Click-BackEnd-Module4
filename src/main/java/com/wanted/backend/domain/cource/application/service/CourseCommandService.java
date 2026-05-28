package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.command.ChangeCourseStatusCommand;
import com.wanted.backend.domain.cource.application.command.CreateCourseCommand;
import com.wanted.backend.domain.cource.application.command.UpdateCourseCommand;
import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;
import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import com.wanted.backend.domain.cource.application.usecase.CourseCommandUseCase;
import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.model.CourseSection;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.Lesson;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import com.wanted.backend.domain.cource.domain.repository.LessonRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final FileProcessingService fileProcessingService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    public Long create(CreateCourseCommand command) {
        Instant now = clock.instant();

        List<CourseSection> sections = command.sections().stream()
                .map(s -> {
                    List<Lesson> lessons = s.lessons().stream()
                            .map(l -> Lesson.create(null, l.title(), l.description(), l.orderIndex(), now))
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
        return saved.getId();
    }

    @Override
    public void update(UpdateCourseCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.getAuthorId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        List<CourseSection> newSections = command.sections().stream()
                .map(sc -> {
                    List<Lesson> lessons = sc.lessons().stream()
                            .map(lc -> lc.id() != null
                                    ? Lesson.restore(lc.id(), null, lc.title(), lc.description(),
                                                     lc.orderIndex(), null, null, null, null)
                                    : Lesson.create(null, lc.title(), lc.description(),
                                                    lc.orderIndex(), clock.instant()))
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
    }

    @Override
    public void changeStatus(ChangeCourseStatusCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

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
    public String uploadLessonVideo(UploadLessonVideoCommand command) {
        Lesson lesson = lessonRepository.findById(command.lessonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        String videoUrl = videoStoragePort.store(
                command.lessonId(),
                command.originalFilename(),
                command.videoData()
        );

        lesson.attachVideo(videoUrl);
        lessonRepository.save(lesson);

        // 트랜잭션 커밋 후 비동기 처리 시작
        Long lessonId = command.lessonId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fileProcessingService.process(lessonId);
            }
        });

        return videoUrl;
    }
}
