package com.wanted.backend.domain.cource.infrastructure.persistence;

import com.wanted.backend.domain.cource.domain.dto.CourseAuthorInfo;
import com.wanted.backend.domain.cource.domain.model.Lesson;
import com.wanted.backend.domain.cource.domain.repository.LessonRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LessonRepositoryAdapter implements LessonRepository {

    private final SpringDataLessonRepository jpaRepository;

    @Transactional
    @Override
    public Lesson save(Lesson lesson) {
        LessonJpaEntity entity = jpaRepository.findById(lesson.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        entity.update(lesson.getVideoUrl(), lesson.getS3Key(), lesson.getFileProcessingStatus());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Lesson> findById(Long lessonId) {
        return jpaRepository.findById(lessonId).map(this::toDomain);
    }

    @Override
    public Optional<CourseAuthorInfo> findCourseAuthorInfo(Long lessonId) {
        return jpaRepository.findCourseAuthorInfoByLessonId(lessonId);
    }

    private Lesson toDomain(LessonJpaEntity entity) {
        return Lesson.restore(
                entity.getId(),
                entity.getSection().getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getOrderIndex(),
                entity.getVideoUrl(),
                entity.getS3Key(),
                entity.getDurationSeconds(),
                entity.getFileProcessingStatus(),
                entity.getCreatedAt()
        );
    }
}
