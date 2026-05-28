package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import com.wanted.backend.domain.enrollment_management.application.port.MyEnrolledCourseQueryPort;
import com.wanted.backend.domain.enrollment_management.domain.model.Enrollment;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import com.wanted.backend.domain.enrollment_management.domain.repository.EnrollmentRepository;
import com.wanted.backend.domain.enrollment_management.infrastructure.course.CourseReferenceEntity;
import com.wanted.backend.domain.enrollment_management.infrastructure.course.CourseReferenceRepository;
import com.wanted.backend.domain.enrollment_management.infrastructure.curriculum.CurriculumReferenceEntity;
import com.wanted.backend.domain.enrollment_management.infrastructure.curriculum.CurriculumReferenceRepository;
import com.wanted.backend.domain.enrollment_management.infrastructure.progress.VideoProgressReferenceEntity;
import com.wanted.backend.domain.enrollment_management.infrastructure.progress.VideoProgressReferenceRepository;
import com.wanted.backend.domain.enrollment_management.infrastructure.video.EnrolledCourseVideoReferenceEntity;
import com.wanted.backend.domain.enrollment_management.infrastructure.video.EnrolledCourseVideoReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyEnrolledCourseQueryAdapter implements MyEnrolledCourseQueryPort {

    // 여러 참조 테이블에서 가져온 조회 결과를 화면 응답에 필요한 포트 데이터로 조합한다.
    private final EnrollmentRepository enrollmentRepository;
    private final CourseReferenceRepository courseRepository;
    private final CurriculumReferenceRepository curriculumRepository;
    private final EnrolledCourseVideoReferenceRepository videoRepository;
    private final VideoProgressReferenceRepository progressRepository;

    @Override
    public List<MyEnrolledCourseData> findByMemberId(Long memberId) {
        List<Enrollment> enrollments = findMyCourseEnrollments(memberId);
        if (enrollments.isEmpty()) {
            return List.of();
        }

        List<Long> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .distinct()
                .toList();

        // 강의/커리큘럼/영상/진도 정보를 한 번씩 조회한 뒤 courseId 기준으로 묶어서 N+1 조회를 피한다.
        Map<Long, CourseReferenceEntity> courseById = courseRepository.findByIdIn(courseIds).stream()
                .collect(Collectors.toMap(CourseReferenceEntity::getId, Function.identity()));
        Map<Long, List<EnrolledCourseVideoReferenceEntity>> videosByCourseId = findVideosByCourseId(courseIds);
        Map<Long, List<VideoProgressReferenceEntity>> progressesByCourseId = progressRepository
                .findByMemberIdAndCourseIdIn(memberId, courseIds).stream()
                .collect(Collectors.groupingBy(VideoProgressReferenceEntity::getCourseId));

        return enrollments.stream()
                .map(enrollment -> toData(
                        enrollment.getCourseId(),
                        courseById.get(enrollment.getCourseId()),
                        videosByCourseId.getOrDefault(enrollment.getCourseId(), List.of()),
                        progressesByCourseId.getOrDefault(enrollment.getCourseId(), List.of())
                ))
                .toList();
    }

    private List<Enrollment> findMyCourseEnrollments(Long memberId) {
        return enrollmentRepository.findByUserId(memberId).stream()
                .filter(enrollment -> EnrollmentStatus.myCourseListTargets().contains(enrollment.getEffectiveStatus()))
                .toList();
    }

    private Map<Long, List<EnrolledCourseVideoReferenceEntity>> findVideosByCourseId(Collection<Long> courseIds) {
        List<CurriculumReferenceEntity> curriculums = curriculumRepository
                .findByCourseIdInOrderByCourseIdAscIdAsc(courseIds);
        if (curriculums.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> courseIdByCurriculumId = curriculums.stream()
                .collect(Collectors.toMap(CurriculumReferenceEntity::getId, CurriculumReferenceEntity::getCourseId));

        return videoRepository.findByCurriculumIdInOrderByCurriculumIdAscIdAsc(courseIdByCurriculumId.keySet()).stream()
                .collect(Collectors.groupingBy(video -> courseIdByCurriculumId.get(video.getCurriculumId())));
    }

    private MyEnrolledCourseData toData(
            Long courseId,
            CourseReferenceEntity course,
            List<EnrolledCourseVideoReferenceEntity> videos,
            List<VideoProgressReferenceEntity> progresses
    ) {
        VideoProgressReferenceEntity lastProgress = findLastProgress(progresses);
        EnrolledCourseVideoReferenceEntity firstVideo = videos.isEmpty() ? null : videos.get(0);

        return new MyEnrolledCourseData(
                courseId,
                course == null ? "(삭제된 강의)" : course.getTitle(),
                course == null ? null : course.getThumbnailUrl(),
                countCompletedLessons(videos, progresses),
                videos.size(),
                lastProgress == null ? null : lastProgress.getUpdatedAt(),
                lastProgress == null ? firstVideoId(firstVideo) : lastProgress.getVideoId(),
                lastProgress == null ? 0 : lastProgress.getLastPositionSeconds()
        );
    }

    private Long firstVideoId(EnrolledCourseVideoReferenceEntity firstVideo) {
        return firstVideo == null ? null : firstVideo.getId();
    }

    private Integer countCompletedLessons(
            List<EnrolledCourseVideoReferenceEntity> videos,
            List<VideoProgressReferenceEntity> progresses
    ) {
        Set<Long> videoIds = videos.stream()
                .map(EnrolledCourseVideoReferenceEntity::getId)
                .collect(Collectors.toSet());

        return (int) progresses.stream()
                .filter(progress -> videoIds.contains(progress.getVideoId()))
                .filter(progress -> Boolean.TRUE.equals(progress.getCompleted()))
                .count();
    }

    private VideoProgressReferenceEntity findLastProgress(List<VideoProgressReferenceEntity> progresses) {
        return progresses.stream()
                .max(Comparator.comparing(VideoProgressReferenceEntity::getUpdatedAt))
                .orElse(null);
    }
}
