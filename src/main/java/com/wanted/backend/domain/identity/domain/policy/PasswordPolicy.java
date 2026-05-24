package com.wanted.backend.domain.identity.domain.policy;

import java.util.regex.Pattern;

public final class PasswordPolicy {

    // 회원가입, 프로필 수정 등에서 동일하게 사용하는 비밀번호 정책
    public static final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@$!%#?&]).{8,16}$";
    public static final String PASSWORD_MESSAGE = "비밀번호는 8자 이상 16자 이하이며 영문, 숫자, 특수문자(@$!%#?&)를 포함해야 합니다";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
