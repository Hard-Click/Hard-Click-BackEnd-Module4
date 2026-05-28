package com.wanted.backend.domain.notice.infrastructure.member;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity(name = "NoticeMemberReference")
@Table(name = "members")
@Immutable
@Getter
public class MemberReferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "role", insertable = false, updatable = false)
    private String role;

    protected MemberReferenceEntity() {}
}