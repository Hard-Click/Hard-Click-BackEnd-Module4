package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.domain.policy.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountLockPasswordChangeRequest {

    @NotBlank(message = "비밀번호 변경 토큰이 필요합니다")
    private String passwordChangeToken;

    @NotBlank(message = "새 비밀번호를 입력해주세요")
    @Pattern(regexp = PasswordPolicy.PASSWORD_REGEX, message = PasswordPolicy.PASSWORD_MESSAGE)
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인을 입력해주세요")
    private String newPasswordConfirm;
}