package com.wanted.backend.domain.enrollment_management.infrastructure.course;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity(name = "EnrollmentCourseReference")
@Getter
@Immutable
@Table(name = "course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseReferenceEntity {

    @Id
    @Column(name = "course_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
}
