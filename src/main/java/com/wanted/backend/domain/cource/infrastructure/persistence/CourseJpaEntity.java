package com.wanted.backend.domain.cource.infrastructure.persistence;

import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.PriceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "course", indexes = {
        @Index(name = "idx_course_status_created_at", columnList = "status, created_at"),
        @Index(name = "idx_course_author_id", columnList = "author_id")
})
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

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
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

    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;

    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;

    @Column(name = "tech_tags", columnDefinition = "TEXT")
    private String techTags;

    @Column(length = 50)
    private String level;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseSectionJpaEntity> sections = new ArrayList<>();

    static CourseJpaEntity from(Long authorId, String title, String subject,
                                String description, String thumbnailUrl,
                                PriceType priceType, int price, CourseStatus status,
                                Instant createdAt,
                                List<String> learningObjectives, List<String> targetAudience,
                                List<String> techTags, String level) {
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
        entity.learningObjectives = toText(learningObjectives);
        entity.targetAudience = toText(targetAudience);
        entity.techTags = toText(techTags);
        entity.level = level;
        return entity;
    }

    void update(String title, String subject, String description,
                String thumbnailUrl, PriceType priceType, int price, CourseStatus status,
                List<String> learningObjectives, List<String> targetAudience,
                List<String> techTags, String level) {
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.priceType = priceType;
        this.price = price;
        this.status = status;
        this.learningObjectives = toText(learningObjectives);
        this.targetAudience = toText(targetAudience);
        this.techTags = toText(techTags);
        this.level = level;
    }

    List<String> getLearningObjectivesList() { return fromText(learningObjectives); }
    List<String> getTargetAudienceList()     { return fromText(targetAudience); }
    List<String> getTechTagsList()           { return fromText(techTags); }

    private static String toText(List<String> items) {
        if (items == null || items.isEmpty()) return null;
        return items.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.replace("\n", " ").replace("\r", " "))
                .collect(Collectors.joining("\n"));
    }

    private static List<String> fromText(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split("\n")).filter(s -> !s.isBlank()).toList();
    }

    CourseSectionJpaEntity addSection(String title, int orderIndex) {
        CourseSectionJpaEntity section = CourseSectionJpaEntity.of(this, title, orderIndex);
        sections.add(section);
        return section;
    }
}
