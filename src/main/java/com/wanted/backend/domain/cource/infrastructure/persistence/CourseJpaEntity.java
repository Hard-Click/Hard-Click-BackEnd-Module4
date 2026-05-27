package com.wanted.backend.domain.cource.infrastructure.persistence;

import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.PriceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    private PriceType priceType;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseSectionJpaEntity> sections = new ArrayList<>();

    static CourseJpaEntity from(Long authorId, String title, String subject,
                                String description, String thumbnailUrl,
                                PriceType priceType, int price, CourseStatus status,
                                Instant createdAt) {
        CourseJpaEntity entity = new CourseJpaEntity();
        entity.authorId = authorId;
        entity.title = title;
        entity.subject = subject;
        entity.description = description;
        entity.thumbnailUrl = thumbnailUrl;
        entity.priceType = priceType;
        entity.price = price;
        entity.status = status;
        entity.createdAt = createdAt;
        return entity;
    }

    void update(String title, String subject, String description,
                String thumbnailUrl, PriceType priceType, int price, CourseStatus status) {
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.priceType = priceType;
        this.price = price;
        this.status = status;
    }

    CourseSectionJpaEntity addSection(String title, int orderIndex) {
        CourseSectionJpaEntity section = CourseSectionJpaEntity.of(this, title, orderIndex);
        sections.add(section);
        return section;
    }
}
