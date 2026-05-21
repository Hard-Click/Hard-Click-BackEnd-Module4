package com.wanted.backend.domain.cource.domain.model;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Course {

    private Long id;
    private Long authorId;
    private String title;
    private String subject;
    private String description;
    private String thumbnailUrl;
    private PriceType priceType;
    private int price;
    private CourseStatus status;
    private List<CourseSection> sections;
    private Instant createdAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Course() {}

    public static Course create(Long authorId, String title, String subject,
                                String description, String thumbnailUrl,
                                PriceType priceType, int price,
                                List<CourseSection> sections, Instant now) {
        validatePrice(priceType, price);
        Course course = new Course();
        course.authorId = authorId;
        course.title = title;
        course.subject = subject;
        course.description = description;
        course.thumbnailUrl = thumbnailUrl;
        course.priceType = priceType;
        course.price = price;
        course.status = CourseStatus.DRAFT;
        course.sections = new ArrayList<>(sections);
        course.createdAt = now;
        return course;
    }

    public static Course restore(Long id, Long authorId, String title, String subject,
                                 String description, String thumbnailUrl,
                                 PriceType priceType, int price, CourseStatus status,
                                 List<CourseSection> sections, Instant createdAt) {
        Course course = new Course();
        course.id = id;
        course.authorId = authorId;
        course.title = title;
        course.subject = subject;
        course.description = description;
        course.thumbnailUrl = thumbnailUrl;
        course.priceType = priceType;
        course.price = price;
        course.status = status;
        course.sections = new ArrayList<>(sections);
        course.createdAt = createdAt;
        return course;
    }

    private static void validatePrice(PriceType priceType, int price) {
        if (priceType == PriceType.FREE && price != 0) {
            throw new IllegalArgumentException("무료 강의의 가격은 0이어야 합니다.");
        }
        if (priceType == PriceType.PAID && price <= 0) {
            throw new IllegalArgumentException("유료 강의의 가격은 0보다 커야 합니다.");
        }
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public PriceType getPriceType() { return priceType; }
    public int getPrice() { return price; }
    public CourseStatus getStatus() { return status; }
    public List<CourseSection> getSections() { return List.copyOf(sections); }
    public Instant getCreatedAt() { return createdAt; }
}
