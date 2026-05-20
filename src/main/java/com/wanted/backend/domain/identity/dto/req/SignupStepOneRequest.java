package com.wanted.backend.domain.identity.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupStepOneRequest {

    @NotBlank(message = "아이디를 입력해주세요")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문, 숫자 조합으로 4자 이상 20자 이하여야 합니다")
    private String username;

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")

    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@$!%#?&]).{8,16}$",
            message = "비밀번호는 8자 이상 16자 이하이며 영문, 숫자, 특수문자(@$!%#?&)를 포함해야 합니다")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요")
    private String passwordConfirm;
}