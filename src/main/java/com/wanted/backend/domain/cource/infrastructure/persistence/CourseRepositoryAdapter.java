package com.wanted.backend.domain.cource.infrastructure.persistence;

import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.model.CourseSection;
import com.wanted.backend.domain.cource.domain.model.Lesson;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CourseRepositoryAdapter implements CourseRepository {

    private final SpringDataCourseRepository jpaRepository;

    @Override
    public Course save(Course course) {
        CourseJpaEntity entity = toJpaEntity(course);
        CourseJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Course> findById(Long courseId) {
        return jpaRepository.findById(courseId).map(this::toDomain);
    }

    private CourseJpaEntity toJpaEntity(Course course) {
        CourseJpaEntity entity = CourseJpaEntity.from(
                course.getAuthorId(),
                course.getTitle(),
                course.getSubject(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getPriceType(),
                course.getPrice(),
                course.getStatus(),
                course.getCreatedAt()
        );
        course.getSections().forEach(section -> {
            CourseSectionJpaEntity sectionEntity = entity.addSection(
                    section.getTitle(), section.getOrderIndex());
            section.getLessons().forEach(lesson ->
                    sectionEntity.addLesson(
                            lesson.getTitle(),
                            lesson.getDescription(),
                            lesson.getOrderIndex(),
                            course.getCreatedAt())
            );
        });
        return entity;
    }

    private Course toDomain(CourseJpaEntity entity) {
        Hibernate.initialize(entity.getSections());

        List<CourseSection> sections = entity.getSections().stream()
                .map(sectionEntity -> {
                    Hibernate.initialize(sectionEntity.getLessons());
                    List<Lesson> lessons = sectionEntity.getLessons().stream()
                            .map(l -> Lesson.restore(
                                    l.getId(),
                                    sectionEntity.getId(),
                                    l.getTitle(),
                                    l.getDescription(),
                                    l.getOrderIndex(),
                                    l.getVideoUrl(),
                                    l.getDurationSeconds(),
                                    l.getCreatedAt()))
                            .toList();
                    return CourseSection.restore(
                            sectionEntity.getId(),
                            sectionEntity.getTitle(),
                            sectionEntity.getOrderIndex(),
                            lessons);
                })
                .toList();

        return Course.restore(
                entity.getId(),
                entity.getAuthorId(),
                entity.getTitle(),
                entity.getSubject(),
                entity.getDescription(),
                entity.getThumbnailUrl(),
                entity.getPriceType(),
                entity.getPrice(),
                entity.getStatus(),
                sections,
                entity.getCreatedAt()
        );
    }
}
