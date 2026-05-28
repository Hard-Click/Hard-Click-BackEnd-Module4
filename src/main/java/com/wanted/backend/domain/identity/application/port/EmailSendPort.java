package com.wanted.backend.domain.identity.application.port;

public interface EmailSendPort {
void sendVerificationCode(String to, String code);
}