package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import com.wanted.backend.domain.enrollment_management.application.port.MyEnrolledCourseQueryPort.MyEnrolledCourseData;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import com.wanted.backend.domain.enrollment_management.infrastructure.course.CourseReferenceEntity;
import com.wanted.backend.domain.enrollment_management.infrastructure.course.CourseReferenceRepository;
import com.wanted.backend.domain.enrollment_management.infrastructure.lesson.EnrolledLessonReferenceEntity;
import com.wanted.backend.domain.enrollment_management.infrastructure.lesson.EnrolledLessonReferenceRepository;
import com.wanted.backend.domain.enrollment_management.infrastructure.progress.VideoProgressReferenceEntity;
import com.wanted.backend.domain.enrollment_management.infrastructure.progress.VideoProgressReferenceRepository;
import com.wanted.backend.domain.enrollment_management.infrastructure.section.EnrolledCourseSectionReferenceEntity;
import com.wanted.backend.domain.enrollment_management.infrastructure.section.EnrolledCourseSectionReferenceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@EntityScan(basePackageClasses = {
        EnrollmentJpaEntity.class,
        CourseReferenceEntity.class,
        EnrolledCourseSectionReferenceEntity.class,
        EnrolledLessonReferenceEntity.class,
        VideoProgressReferenceEntity.class
})
@EnableJpaRepositories(basePackageClasses = {
        SpringDataEnrollmentRepository.class,
        CourseReferenceRepository.class,
        EnrolledCourseSectionReferenceRepository.class,
        EnrolledLessonReferenceRepository.class,
        VideoProgressReferenceRepository.class
})
@Import({
        EnrollmentRepositoryAdapter.class,
        MyEnrolledCourseQueryAdapter.class
})
@Sql(scripts = {
        "/sql/enrollment_management_adapter_schema.sql",
        "/sql/enrollment_management_adapter_data.sql"
})
class MyEnrolledCourseQueryAdapterTest {

    @Autowired
    private MyEnrolledCourseQueryAdapter queryAdapter;

    @Test
    void 강의별로_레슨_순서를_보존하며_마지막_시청_위치를_반환한다() {
        List<MyEnrolledCourseData> result = queryAdapter.findByMemberId(1L);

        assertThat(result).hasSize(1);
        MyEnrolledCourseData data = result.get(0);
        assertThat(data.courseId()).isEqualTo(20L);
        assertThat(data.courseTitle()).isEqualTo("Spring Course");
        assertThat(data.totalLessonCount()).isEqualTo(2);
        assertThat(data.completedLessonCount()).isEqualTo(1);
        assertThat(data.lastVideoId()).isEqualTo(10L);
        assertThat(data.lastPositionSeconds()).isEqualTo(42);
        assertThat(data.enrollmentStatus()).isEqualTo(EnrollmentStatus.IN_PROGRESS);
    }
}
