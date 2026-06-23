package com.wanted.backend.domain.wishlist.infrastructure.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity(name = "WishlistReview")
@Getter
@Table(name = "reviews")
public class WishlistReviewJpaEntity {

    @Id
    @Column(name = "review_id")
    private Long id;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "rating")
    private Integer rating;
}
