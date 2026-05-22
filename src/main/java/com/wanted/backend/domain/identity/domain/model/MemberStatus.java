package com.wanted.backend.domain.identity.domain.model;

public enum MemberStatus {
    ACTIVE,                 // 정상 활성 상태
    SUSPENDED,              // 정지 상태
    BANNED,                 // 영구 정지
    WITHDRAWN               // 탈퇴
}