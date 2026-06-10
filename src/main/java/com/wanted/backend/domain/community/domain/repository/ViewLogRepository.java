package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.ViewLog;

import java.time.LocalDateTime;

public interface ViewLogRepository {

    ViewLog save(ViewLog viewLog);

    boolean existsByMemberIdAndPostIdAndViewedAtAfter(
            Long memberId, Long postId, LocalDateTime after);
}