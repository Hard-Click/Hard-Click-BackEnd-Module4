package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.domain.policy.PasswordPolicy;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "아이디를 입력해주세요")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문, 숫자 조합으로 4자 이상 20자 이하여야 합니다")
    private String username;

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(regexp = PasswordPolicy.PASSWORD_REGEX, message = PasswordPolicy.PASSWORD_MESSAGE)
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요")
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력해주세요")
    private String name;

    @NotBlank(message = "성별을 선택해주세요")
    private String gender;

    @NotNull(message = "생년월일을 입력해주세요")
    private LocalDate birthDate;

    @NotBlank(message = "전화번호를 입력해주세요")
    private String phoneNumber;

    private String profileImageUrl;

    @NotBlank(message = "이메일 인증이 필요합니다")
    private String emailVerificationToken;

}