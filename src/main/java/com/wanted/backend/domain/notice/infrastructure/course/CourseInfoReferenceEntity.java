package com.wanted.backend.domain.notice.infrastructure.course;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;


@Entity(name = "NoticeCourseInfoReference")
@Table(name = "course")
@Immutable
@Getter
public class CourseInfoReferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "title", insertable = false, updatable = false)
    private String title;

    protected CourseInfoReferenceEntity() {}
}