package com.wanted.backend.domain.order.infrastructure.enrollment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity(name = "OrderEnrollmentRef")
@Getter
@Immutable
@Table(name = "enrollment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEnrollmentRefEntity {

    @Id
    @Column(name = "enrollment_id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "status")
    private String status;
}
