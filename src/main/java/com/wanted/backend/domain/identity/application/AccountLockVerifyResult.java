package com.wanted.backend.domain.identity.application;

public record AccountLockVerifyResult(String passwordChangeToken) {
    public static AccountLockVerifyResult from(String passwordChangeToken) {
        return new AccountLockVerifyResult(passwordChangeToken);
    }
}
