package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.dto.CourseDetailResult;
import com.wanted.backend.domain.cource.application.port.InstructorQueryPort;
import com.wanted.backend.domain.cource.application.usecase.GetCourseDetailUseCase;
import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCourseDetailService implements GetCourseDetailUseCase {

    private final CourseRepository courseRepository;
    private final InstructorQueryPort instructorQueryPort;

    @Override
    public CourseDetailResult handle(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        Map<Long, String> nameMap = instructorQueryPort.findNamesByIds(List.of(course.getAuthorId()));
        String instructorName = nameMap.getOrDefault(course.getAuthorId(), "알 수 없음");

        List<CourseDetailResult.SectionResult> sections = course.getSections().stream()
                .sorted(Comparator.comparingInt(s -> s.getOrderIndex()))
                .map(section -> new CourseDetailResult.SectionResult(
                        section.getId(),
                        section.getTitle(),
                        section.getOrderIndex(),
                        section.getLessons().stream()
                                .sorted(Comparator.comparingInt(l -> l.getOrderIndex()))
                                .map(lesson -> new CourseDetailResult.LessonResult(
                                        lesson.getId(),
                                        lesson.getTitle(),
                                        lesson.getDescription(),
                                        lesson.getOrderIndex(),
                                        lesson.getDurationSeconds(),
                                        section.getOrderIndex() == 0 && lesson.getOrderIndex() == 0
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return new CourseDetailResult(
                course.getId(),
                course.getTitle(),
                course.getSubject(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getPriceType(),
                course.getPrice(),
                course.getStatus(),
                instructorName,
                0.0,
                0,
                0,
                sections,
                course.getLearningObjectives(),
                course.getTargetAudience(),
                course.getTechTags(),
                course.getLevel()
        );
    }
}
