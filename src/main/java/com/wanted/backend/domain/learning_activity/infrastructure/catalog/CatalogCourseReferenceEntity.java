package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@Table(name = "course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogCourseReferenceEntity {

    @Id
    @Column(name = "course_id")
    private Long id;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer price;
}
