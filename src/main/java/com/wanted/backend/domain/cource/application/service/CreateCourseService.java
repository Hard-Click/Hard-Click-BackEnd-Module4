package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.command.CreateCourseCommand;
import com.wanted.backend.domain.cource.application.usecase.CreateCourseUseCase;
import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.model.CourseSection;
import com.wanted.backend.domain.cource.domain.model.Lesson;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateCourseService implements CreateCourseUseCase {

    private final CourseRepository courseRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    @Override
    public Long handle(CreateCourseCommand command) {
        // clock : 테스트하기 좋게 설계된 방식으로, 시스템의 현재 정확한 순간(UTC 기준)을 가져오는 코드
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

        return saved.getId();
    }
}
