package com.wanted.backend.domain.identity.domain.policy;

import java.util.Locale;

public final class EmailPolicy {

    private EmailPolicy() {
    }

    public static boolean isAllowedDomain(String email, String allowedDomain) {
        if (email == null || allowedDomain == null || allowedDomain.isBlank()) {
            return false;
        }

        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        String normalizedDomain = allowedDomain.toLowerCase(Locale.ROOT);
        String domainSuffix = normalizedDomain.startsWith("@") ? normalizedDomain : "@" + normalizedDomain;

        return normalizedEmail.endsWith(domainSuffix);
    }
}
