package com.wanted.backend.domain.subject.application.usecase;

import com.wanted.backend.domain.subject.domain.model.Subject;

import java.util.List;

public interface GetSubjectListUseCase {
    List<Subject> handle();
}
