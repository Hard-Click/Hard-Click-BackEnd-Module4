package com.wanted.backend.domain.community.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "view_logs")
@Getter
public class ViewLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_log_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    protected ViewLogJpaEntity() {}

    public ViewLogJpaEntity(Long memberId, Long postId, LocalDateTime viewedAt) {
        this.memberId = memberId;
        this.postId = postId;
        this.viewedAt = viewedAt;
    }
}