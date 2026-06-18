package com.wanted.backend.domain.study_timer.application.port;

public interface MemberLockPort {

    void lock(Long memberId);
}
