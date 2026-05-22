package com.wanted.backend.domain.subject.infrastructure.persistence;

import com.wanted.backend.domain.subject.domain.model.Subject;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subjects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubjectJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Subject toDomain() {
        return Subject.restore(id, name);
    }
}
