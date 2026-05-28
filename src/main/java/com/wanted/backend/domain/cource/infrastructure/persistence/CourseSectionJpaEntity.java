package com.wanted.backend.domain.cource.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseSectionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseJpaEntity course;

    @Column(nullable = false)
    private String title;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonJpaEntity> lessons = new ArrayList<>();

    static CourseSectionJpaEntity of(CourseJpaEntity course, String title, int orderIndex) {
        CourseSectionJpaEntity entity = new CourseSectionJpaEntity();
        entity.course = course;
        entity.title = title;
        entity.orderIndex = orderIndex;
        return entity;
    }

    void update(String title, int orderIndex) {
        this.title = title;
        this.orderIndex = orderIndex;
    }

    void addLesson(String title, String description, int orderIndex, Instant createdAt) {
        lessons.add(LessonJpaEntity.of(this, title, description, orderIndex, createdAt));
    }
}
