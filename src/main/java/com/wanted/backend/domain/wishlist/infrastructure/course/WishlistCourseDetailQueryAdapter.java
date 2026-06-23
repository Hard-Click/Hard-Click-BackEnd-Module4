package com.wanted.backend.domain.wishlist.infrastructure.course;

import com.wanted.backend.domain.wishlist.application.port.WishlistCourseDetailQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WishlistCourseDetailQueryAdapter implements WishlistCourseDetailQueryPort {

    private final WishlistCourseJpaRepository courseRepository;
    private final WishlistMemberJpaRepository memberRepository;
    private final WishlistReviewJpaRepository reviewRepository;
    private final WishlistEnrollmentCheckRepository enrollmentRepository;
    private final WishlistCartCheckRepository cartRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDetail> findAllByIds(List<Long> courseIds, Long memberId) {
        if (courseIds.isEmpty()) {
            return List.of();
        }

        List<WishlistCourseJpaEntity> courses = courseRepository.findAllByIdIn(courseIds);

        List<Long> authorIds = courses.stream().map(WishlistCourseJpaEntity::getAuthorId).distinct().toList();
        Map<Long, String> authorNameMap = memberRepository.findAllByIdIn(authorIds).stream()
                .collect(Collectors.toMap(WishlistMemberJpaEntity::getId, WishlistMemberJpaEntity::getName));

        Map<Long, double[]> ratingMap = buildRatingMap(courseIds);

        Set<Long> enrolledCourseIds = Set.copyOf(enrollmentRepository.findEnrolledCourseIds(memberId, courseIds));
        Set<Long> inCartCourseIds = Set.copyOf(cartRepository.findInCartCourseIds(memberId, courseIds));

        return courses.stream().map(course -> new CourseDetail(
                course.getId(),
                course.getTitle(),
                authorNameMap.getOrDefault(course.getAuthorId(), ""),
                course.getPrice(),
                ratingMap.getOrDefault(course.getId(), new double[]{0.0, 0})[0],
                (int) ratingMap.getOrDefault(course.getId(), new double[]{0.0, 0})[1],
                enrolledCourseIds.contains(course.getId()),
                inCartCourseIds.contains(course.getId())
        )).toList();
    }

    private Map<Long, double[]> buildRatingMap(List<Long> courseIds) {
        return reviewRepository.findRatingStatsByCourseIds(courseIds).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> new double[]{
                                row[1] != null ? ((Number) row[1]).doubleValue() : 0.0,
                                row[2] != null ? ((Number) row[2]).doubleValue() : 0.0
                        }
                ));
    }
}
