package com.wanted.backend.domain.identity.domain.model;

public enum EmailPurpose {
    SIGNUP,          // 회원가입 인증
    ACCOUNT_LOCK,    // 계정 잠금 복구 인증
    PASSWORD_RESET   // 비밀번호 찾기 인증
}