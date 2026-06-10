package com.wanted.backend.domain.subject.domain.repository;

import com.wanted.backend.domain.subject.domain.model.Subject;

import java.util.List;

public interface SubjectRepository {
    List<Subject> findAll();
}
