package com.wanted.backend.domain.enrollment_management.infrastructure.course;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity(name = "EnrollmentCourseReference")
@Getter
@Immutable
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseReferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
}
