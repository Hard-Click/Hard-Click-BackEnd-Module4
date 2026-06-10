package com.wanted.backend.domain.subject.infrastructure.persistence;

import com.wanted.backend.domain.subject.domain.model.Subject;
import com.wanted.backend.domain.subject.domain.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubjectRepositoryAdapter implements SubjectRepository {

    private final SpringDataSubjectRepository jpaRepository;

    @Override
    public List<Subject> findAll() {
        return jpaRepository.findAll().stream()
                .map(SubjectJpaEntity::toDomain)
                .toList();
    }
}
