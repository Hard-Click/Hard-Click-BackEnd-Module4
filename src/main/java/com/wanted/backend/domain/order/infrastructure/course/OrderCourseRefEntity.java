package com.wanted.backend.domain.order.infrastructure.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity(name = "OrderCourseRef")
@Getter
@Immutable
@Table(name = "course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCourseRefEntity {

    @Id
    @Column(name = "course_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private Integer price;

    @Column(name = "status")
    private String status;
}
