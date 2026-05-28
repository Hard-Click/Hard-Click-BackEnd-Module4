package com.wanted.backend.domain.community.infrastructure.member;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "members")
@Immutable
@Getter
public class MemberReferenceEntity {

    @Id
    @Column(name = "member_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    protected MemberReferenceEntity() {

    }
}