package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.AccountLockVerifyResult;
import com.wanted.backend.domain.identity.application.command.AccountLockPasswordChangeCommand;
import com.wanted.backend.domain.identity.application.command.AccountLockVerifyCommand;
import com.wanted.backend.domain.identity.application.usecase.EmailVerificationUseCase;
import com.wanted.backend.domain.identity.application.usecase.PasswordCommandUseCase;
import com.wanted.backend.domain.identity.presentation.api.request.AccountLockEmailRequest;
import com.wanted.backend.domain.identity.presentation.api.request.AccountLockPasswordChangeRequest;
import com.wanted.backend.domain.identity.presentation.api.request.AccountLockVerifyRequest;
import com.wanted.backend.domain.identity.presentation.api.response.EmptyResponse;
import com.wanted.backend.domain.identity.presentation.api.response.PasswordChangeTokenResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Account Lock", description = "계정 잠금 해제 API")
@RestController
@RequestMapping("/api/auth/account-locks")
@RequiredArgsConstructor
public class AccountLockController {

    private final PasswordCommandUseCase passwordCommandUseCase;
    private final EmailVerificationUseCase emailVerificationUseCase;

    @Operation(
            summary = "계정 잠금 인증번호 발송",
            description = "계정 잠금 상태의 사용자 이메일로 인증번호를 발송합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이메일 형식 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가입된 이메일이 없거나 계정 잠금 상태가 아님")
    })
    @PostMapping("/email")
    public ResponseEntity<ApiResponse<EmptyResponse>> sendEmail(
            @Valid @RequestBody AccountLockEmailRequest request
    ) {
        emailVerificationUseCase.sendAccountLockCode(request.email());
        return ApiResponse.success("계정 잠금 인증번호가 발송되었습니다", new EmptyResponse());
    }

    @Operation(
            summary = "계정 잠금 인증",
            description = "계정 잠금 상태의 사용자가 이메일 인증번호를 검증하고 비밀번호 변경 토큰을 발급받습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공 및 비밀번호 변경 토큰 발급"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증번호 불일치 또는 만료")
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PasswordChangeTokenResponse>> verify(
            @Valid @RequestBody AccountLockVerifyRequest request
    ) {
        AccountLockVerifyResult result = passwordCommandUseCase.verify(new AccountLockVerifyCommand(
                request.email(),
                request.code()
        ));

        return ApiResponse.success(
                "계정 보호 인증이 완료되었습니다",
                new PasswordChangeTokenResponse(result.passwordChangeToken())
        );
    }

    @Operation(
            summary = "잠금 계정 비밀번호 변경",
            description = "계정 잠금 인증 후 발급받은 비밀번호 변경 토큰으로 새 비밀번호를 설정하고 계정 잠금을 해제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 및 계정 잠금 해제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "비밀번호 변경 토큰 유효하지 않거나 비밀번호 정책 미충족")
    })
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<EmptyResponse>> changePassword(
            @Valid @RequestBody AccountLockPasswordChangeRequest request
    ) {
        passwordCommandUseCase.changePassword(new AccountLockPasswordChangeCommand(
                request.passwordChangeToken(),
                request.newPassword(),
                request.newPasswordConfirm()
        ));

        return ApiResponse.success(
                "비밀번호가 변경되고 계정 잠금이 해제되었습니다",
                new EmptyResponse()
        );
    }
}