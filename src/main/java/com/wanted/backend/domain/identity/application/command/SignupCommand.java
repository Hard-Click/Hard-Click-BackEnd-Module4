package com.wanted.backend.domain.identity.application.command;

import java.time.LocalDate;

public record SignupCommand(
        String username,
        String email,
        String password,
        String name,
        String gender,
        LocalDate birthDate,
        String phoneNumber,
        String profileImageUrl,
        String emailVerificationToken,
        Boolean optionalTermsAgreed
) {
}