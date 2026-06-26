package com.wanted.backend.domain.wishlist.infrastructure.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity(name = "WishlistMember")
@Getter
@Table(name = "members")
public class WishlistMemberJpaEntity {

    @Id
    @Column(name = "member_id")
    private Long id;

    @Column(name = "name")
    private String name;
}
