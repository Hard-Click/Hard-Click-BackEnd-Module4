package com.wanted.backend.domain.identity.domain.model;

public enum MemberStatus {
    ACTIVE,                 // 정상 활성 상태
    COMMUNITY_RESTRICTED,   // 커뮤니티 제한
    SUSPENDED,              // 정지 상태
    BANNED,                 // 영구 정지
    WITHDRAWN               // 탈퇴
}