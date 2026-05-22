package com.wanted.backend.domain.cource.domain.model;

import java.util.ArrayList;
import java.util.List;

public class CourseSection {

    private Long id;
    private String title;
    private int orderIndex;
    private List<Lesson> lessons;

    private CourseSection() {}

    public static CourseSection create(String title, int orderIndex, List<Lesson> lessons) {
        CourseSection section = new CourseSection();
        section.title = title;
        section.orderIndex = orderIndex;
        section.lessons = new ArrayList<>(lessons);
        return section;
    }

    public static CourseSection restore(Long id, String title, int orderIndex, List<Lesson> lessons) {
        CourseSection section = new CourseSection();
        section.id = id;
        section.title = title;
        section.orderIndex = orderIndex;
        section.lessons = new ArrayList<>(lessons);
        return section;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getOrderIndex() { return orderIndex; }
    public List<Lesson> getLessons() { return List.copyOf(lessons); }
}
