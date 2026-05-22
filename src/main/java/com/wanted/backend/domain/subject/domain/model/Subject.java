package com.wanted.backend.domain.subject.domain.model;

public class Subject {

    private Long id;
    private String name;

    private Subject() {}

    public static Subject restore(Long id, String name) {
        Subject subject = new Subject();
        subject.id = id;
        subject.name = name;
        return subject;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
