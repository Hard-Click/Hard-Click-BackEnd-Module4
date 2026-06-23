package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.dto.CourseDetailResult;
import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.application.dto.InstructorDashboardResult;
import com.wanted.backend.domain.cource.application.port.EnrollmentStatsPort;
import com.wanted.backend.domain.cource.application.port.InstructorQueryPort;
import com.wanted.backend.domain.cource.application.port.InstructorStatsPort;
import com.wanted.backend.domain.cource.application.port.ReviewStatsPort;
import com.wanted.backend.domain.cource.application.query.CourseListQuery;
import com.wanted.backend.domain.cource.application.usecase.CourseQueryUseCase;
import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.model.CourseListItem;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.PageResult;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseQueryService implements CourseQueryUseCase {

    // 강사 대시보드 "퀴즈 수" 카드 - quiz 도메인이 Mock API라 임시 고정값 사용
    private static final int MOCK_QUIZ_COUNT = 36;

    private final CourseRepository courseRepository;
    private final InstructorQueryPort instructorQueryPort;
    private final ReviewStatsPort reviewStatsPort;
    private final EnrollmentStatsPort enrollmentStatsPort;
    private final InstructorStatsPort instructorStatsPort;

    @Override
    public CourseListResult getList(CourseListQuery query) {
        List<Long> authorIds = null;
        if (query.instructorName() != null && !query.instructorName().isBlank()) {
            authorIds = instructorQueryPort.findIdsByName(query.instructorName());
            if (authorIds.isEmpty()) {
                return new CourseListResult(Collections.emptyList(), query.page(), 0, 0);
            }
        }

        PageResult<CourseListItem> pageResult = courseRepository.findList(
                query.keyword(), query.subject(), authorIds, query.sort(), query.page(), query.size());

        List<Long> ids = pageResult.content().stream()
                .map(CourseListItem::authorId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> nameMap = ids.isEmpty() ? Map.of() : instructorQueryPort.findNamesByIds(ids);

        List<Long> courseIds = pageResult.content().stream()
                .map(CourseListItem::courseId)
                .collect(Collectors.toList());
        Map<Long, ReviewStatsPort.Stats> reviewStatsMap = reviewStatsPort.findStatsByCourseIds(courseIds);
        Map<Long, Integer> enrollmentCountMap = enrollmentStatsPort.countByCourseIds(courseIds);

        List<CourseListResult.Item> items = pageResult.content().stream()
                .map(item -> {
                    ReviewStatsPort.Stats stats = reviewStatsMap.getOrDefault(item.courseId(), ReviewStatsPort.Stats.EMPTY);
                    return new CourseListResult.Item(
                            item.courseId(), item.title(), item.subject(), item.thumbnailUrl(),
                            item.priceType(), item.price(),
                            nameMap.getOrDefault(item.authorId(), "알 수 없음"),
                            stats.avgRating(),
                            stats.reviewCount(),
                            enrollmentCountMap.getOrDefault(item.courseId(), 0),
                            item.status(), item.createdAt()
                    );
                })
                .collect(Collectors.toList());

        // POPULAR/RATING은 집계값 기반 인메모리 정렬
        if (query.sort() == com.wanted.backend.domain.cource.domain.model.CourseSortType.POPULAR) {
            items = items.stream()
                    .sorted(Comparator.comparingInt(CourseListResult.Item::studentCount).reversed())
                    .collect(Collectors.toList());
        } else if (query.sort() == com.wanted.backend.domain.cource.domain.model.CourseSortType.RATING) {
            items = items.stream()
                    .sorted(Comparator.comparingDouble(CourseListResult.Item::rating).reversed())
                    .collect(Collectors.toList());
        }

        return new CourseListResult(items, pageResult.currentPage(),
                pageResult.totalPages(), pageResult.totalCount());
    }

    @Override
    public CourseDetailResult getDetail(Long courseId, Long requesterId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            if (requesterId == null || !requesterId.equals(course.getAuthorId())) {
                throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
            }
        }

        Map<Long, String> nameMap = instructorQueryPort.findNamesByIds(List.of(course.getAuthorId()));
        String instructorName = nameMap.getOrDefault(course.getAuthorId(), "알 수 없음");
        InstructorQueryPort.InstructorProfile instructorProfile = instructorQueryPort.findProfileById(course.getAuthorId());

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
                course.getId(), course.getTitle(), course.getSubject(),
                course.getDescription(), course.getThumbnailUrl(),
                course.getPriceType(), course.getPrice(), course.getStatus(),
                instructorName,
                reviewStatsPort.avgRating(course.getId()),
                reviewStatsPort.reviewCount(course.getId()),
                enrollmentStatsPort.enrollmentCount(course.getId()),
                instructorStatsPort.totalStudents(course.getAuthorId()),
                instructorStatsPort.totalCourses(course.getAuthorId()),
                instructorStatsPort.avgRating(course.getAuthorId()),
                instructorProfile.oneLineIntro(),
                instructorProfile.introduction(),
                instructorProfile.career(),
                sections,
                course.getLearningObjectives(), course.getTargetAudience(),
                course.getTechTags(), course.getLevel()
        );
    }

    @Override
    public CourseListResult getInstructorCourses(Long instructorId, int page, int size) {
        PageResult<CourseListItem> pageResult = courseRepository.findByAuthor(instructorId, page, size);

        List<Long> ids = pageResult.content().stream()
                .map(CourseListItem::authorId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> nameMap = ids.isEmpty() ? Map.of() : instructorQueryPort.findNamesByIds(ids);

        List<Long> courseIds = pageResult.content().stream()
                .map(CourseListItem::courseId)
                .collect(Collectors.toList());
        Map<Long, ReviewStatsPort.Stats> reviewStatsMap = reviewStatsPort.findStatsByCourseIds(courseIds);
        Map<Long, Integer> enrollmentCountMap = enrollmentStatsPort.countByCourseIds(courseIds);

        List<CourseListResult.Item> items = pageResult.content().stream()
                .map(item -> {
                    ReviewStatsPort.Stats stats = reviewStatsMap.getOrDefault(item.courseId(), ReviewStatsPort.Stats.EMPTY);
                    return new CourseListResult.Item(
                            item.courseId(), item.title(), item.subject(), item.thumbnailUrl(),
                            item.priceType(), item.price(),
                            nameMap.getOrDefault(item.authorId(), "알 수 없음"),
                            stats.avgRating(),
                            stats.reviewCount(),
                            enrollmentCountMap.getOrDefault(item.courseId(), 0),
                            item.status(), item.createdAt()
                    );
                })
                .collect(Collectors.toList());

        return new CourseListResult(items, pageResult.currentPage(),
                pageResult.totalPages(), pageResult.totalCount());
    }

    @Override
    public InstructorDashboardResult getInstructorDashboard(Long instructorId) {
        InstructorStatsPort.CourseCounts counts = instructorStatsPort.courseCounts(instructorId);
        int totalStudents = instructorStatsPort.totalStudents(instructorId);

        return new InstructorDashboardResult(
                counts.total(), counts.published(), counts.hidden(),
                totalStudents, MOCK_QUIZ_COUNT
        );
    }
}
