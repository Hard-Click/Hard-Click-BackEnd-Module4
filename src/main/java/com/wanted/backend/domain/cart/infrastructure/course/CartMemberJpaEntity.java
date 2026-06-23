package com.wanted.backend.domain.cart.infrastructure.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity(name = "CartMember")
@Getter
@Immutable
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartMemberJpaEntity {

    @Id
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;
}
