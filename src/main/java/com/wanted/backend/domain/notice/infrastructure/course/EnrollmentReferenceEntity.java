package com.wanted.backend.domain.notice.infrastructure.course;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity(name = "NoticeEnrollmentReference")
@Table(name = "enrollment")
@Immutable
@Getter
public class EnrollmentReferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "member_id", insertable = false, updatable = false)
    private Long memberId;

    @Column(name = "course_id", insertable = false, updatable = false)
    private Long courseId;

    protected EnrollmentReferenceEntity() {}
}