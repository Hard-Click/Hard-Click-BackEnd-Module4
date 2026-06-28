package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.command.ResetPasswordCommand;
import com.wanted.backend.domain.identity.application.usecase.EmailVerificationUseCase;
import com.wanted.backend.domain.identity.application.usecase.PasswordCommandUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.presentation.api.request.PasswordResetEmailRequest;
import com.wanted.backend.domain.identity.presentation.api.request.PasswordResetVerifyRequest;
import com.wanted.backend.domain.identity.presentation.api.request.ResetPasswordRequest;
import com.wanted.backend.domain.identity.presentation.api.response.EmptyResponse;
import com.wanted.backend.domain.identity.presentation.api.response.PasswordChangeTokenResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Identity", description = "인증 및 회원가입 API")
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final EmailVerificationUseCase emailVerificationUseCase;
    private final PasswordCommandUseCase passwordCommandUseCase;

    @Operation(
            summary = "비밀번호 재설정 인증번호 발송",
            description = "가입된 이메일로 비밀번호 재설정 인증번호를 발송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증번호 발송 성공"),
            @ApiResponse(responseCode = "400", description = "이메일 형식 오류"),
            @ApiResponse(responseCode = "404", description = "가입된 이메일이 없음")
    })
    @PostMapping("/email")
    public ResponseEntity<ApiResponse<EmptyResponse>> sendResetCode(
            @Valid @RequestBody PasswordResetEmailRequest request
    ) {
        emailVerificationUseCase.sendPasswordResetCode(request.email());

        return ApiResponse.success(
                "비밀번호 재설정 인증번호가 발송되었습니다",
                new EmptyResponse()
        );
    }
    @Operation(
            summary = "비밀번호 재설정 인증번호 검증",
            description = "이메일로 발송된 인증번호를 검증하고 비밀번호 변경 토큰을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증번호 검증 성공 및 비밀번호 변경 토큰 발급"),
            @ApiResponse(responseCode = "400", description = "인증번호 불일치 또는 만료")
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PasswordChangeTokenResponse>> verifyResetCode(
            @Valid @RequestBody PasswordResetVerifyRequest request
    ) {
        String token = emailVerificationUseCase.verifyCode(
                request.email(),
                request.code(),
                EmailPurpose.PASSWORD_RESET
        );

        return ApiResponse.success(
                "인증번호 검증이 완료되었습니다",
                new PasswordChangeTokenResponse(token)
        );
    }
    @Operation(
            summary = "비밀번호 재설정",
            description = "비밀번호 변경 토큰을 사용해 새 비밀번호로 재설정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 변경 토큰 유효하지 않거나 비밀번호 정책 미충족")
    })
    @PatchMapping
    public ResponseEntity<ApiResponse<EmptyResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordCommandUseCase.resetPassword(new ResetPasswordCommand(
                request.email(),
                request.passwordChangeToken(),
                request.newPassword(),
                request.newPasswordConfirm()
        ));

        return ApiResponse.success(
                "비밀번호가 성공적으로 재설정되었습니다",
                new EmptyResponse()
        );
    }
}
