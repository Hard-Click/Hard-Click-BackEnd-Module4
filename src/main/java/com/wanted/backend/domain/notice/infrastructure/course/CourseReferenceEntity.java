package com.wanted.backend.domain.notice.infrastructure.course;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;


@Entity(name = "NoticeCourseReference")
@Table(name = "courses")
@Immutable //읽기 전용 테이블
@Getter
public class CourseReferenceEntity {

    @Id
    @Column(name = "course_id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "author_id", insertable = false, updatable = false)
    private Long authorId;

    protected CourseReferenceEntity() {

    }
}