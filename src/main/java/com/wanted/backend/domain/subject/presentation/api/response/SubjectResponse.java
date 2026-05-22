package com.wanted.backend.domain.subject.presentation.api.response;

import com.wanted.backend.domain.subject.domain.model.Subject;

public record SubjectResponse(
        Long subjectId,
        String subjectName
) {
    public static SubjectResponse from(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName());
    }
}
