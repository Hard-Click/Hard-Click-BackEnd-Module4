package com.wanted.backend.domain.ranking.application.port;

public interface MemberStreakPort {

    int getCurrentStreakDays(Long memberId);
}
