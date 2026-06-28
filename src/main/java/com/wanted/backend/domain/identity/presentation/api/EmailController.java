ackage com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.usecase.EmailVerificationUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.presentation.api.request.EmailSendRequest;
import com.wanted.backend.domain.identity.presentation.api.request.EmailVerifyRequest;
import com.wanted.backend.domain.identity.presentation.api.response.EmailVerifyResponse;
import com.wanted.backend.domain.identity.presentation.api.response.EmptyResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Email Verification", description = "회원가입 이메일 인증 API")
@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerificationUseCase emailVerificationUseCase;


    @Operation(
            summary = "회원가입 이메일 인증번호 발송",
            description = "회원가입에 사용할 이메일로 인증번호를 발송합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이메일 형식 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    })
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<EmptyResponse>> sendCode(
            @Valid @RequestBody EmailSendRequest request
    ) {
        emailVerificationUseCase.sendVerificationCode(request.email(), EmailPurpose.SIGNUP);
        return ApiResponse.success("이메일 인증번호가 발송되었습니다", new EmptyResponse());
    }


    @Operation(
            summary = "회원가입 이메일 인증번호 검증",
            description = "이메일로 발송된 인증번호를 검증하고 회원가입에 사용할 이메일 인증 토큰을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 검증 성공 및 토큰 발급"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증번호 불일치 또는 만료")
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> verifyCode(
            @Valid @RequestBody EmailVerifyRequest request
    ) {
        String token = emailVerificationUseCase.verifyCode(
                request.email(),
                request.code(),
                EmailPurpose.SIGNUP
        );

        return ApiResponse.success(
                "이메일 인증이 완료되었습니다",
                new EmailVerifyResponse(token)
        );
    }
}
