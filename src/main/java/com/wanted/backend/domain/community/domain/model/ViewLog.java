package com.wanted.backend.domain.community.domain.model;

import java.time.LocalDateTime;

public class ViewLog {

    private Long id;
    private Long memberId;
    private Long postId;
    private LocalDateTime viewedAt;

    private ViewLog(Long id, Long memberId, Long postId, LocalDateTime viewedAt) {
        this.id = id;
        this.memberId = memberId;
        this.postId = postId;
        this.viewedAt = viewedAt;
    }

    public static ViewLog create(Long memberId, Long postId) {
        return new ViewLog(null, memberId, postId, LocalDateTime.now());
    }

    public static ViewLog restore(Long id, Long memberId, Long postId, LocalDateTime viewedAt) {
        return new ViewLog(id, memberId, postId, viewedAt);
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getPostId() { return postId; }
    public LocalDateTime getViewedAt() { return viewedAt; }
}