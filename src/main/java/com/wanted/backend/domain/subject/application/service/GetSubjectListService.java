package com.wanted.backend.domain.subject.application.service;

import com.wanted.backend.domain.subject.application.usecase.GetSubjectListUseCase;
import com.wanted.backend.domain.subject.domain.model.Subject;
import com.wanted.backend.domain.subject.domain.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSubjectListService implements GetSubjectListUseCase {

    private final SubjectRepository subjectRepository;

    @Override
    public List<Subject> handle() {
        return subjectRepository.findAll();
    }
}
