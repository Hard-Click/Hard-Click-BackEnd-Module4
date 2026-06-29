package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.command.UpdateMyProfileCommand;
import com.wanted.backend.domain.identity.application.usecase.PasswordCommandUseCase;
import com.wanted.backend.domain.identity.application.usecase.ProfileCommandUseCase;
import com.wanted.backend.domain.identity.application.usecase.ProfileQueryUseCase;
import com.wanted.backend.domain.identity.presentation.api.request.UpdateMyProfileRequest;
import com.wanted.backend.domain.identity.presentation.api.request.UpdatePasswordRequest;
import com.wanted.backend.domain.identity.presentation.api.request.VerifyPasswordRequest;
import com.wanted.backend.domain.identity.presentation.api.request.WithdrawMemberRequest;
import com.wanted.backend.domain.identity.presentation.api.response.EmptyResponse;
import com.wanted.backend.domain.identity.presentation.api.response.ProfileImageResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "User Profile", description = "마이페이지 프로필 패스워드 확인 API")
public class UserProfileController {

    private final ProfileQueryUseCase profileQueryUseCase;
    private final ProfileCommandUseCase profileCommandUseCase;
    private final PasswordCommandUseCase passwordCommandUseCase;

    @GetMapping("/me")
    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 사용자의 아이디, 이름, 이메일, 프로필 이미지를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<ProfileQueryUseCase.MyProfileView>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                "내 프로필이 조회되었습니다.",
                profileQueryUseCase.handle(userDetails.getMemberId())
        );
    }

    @PatchMapping("/me/password")
    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 새 비밀번호 정책 미충족"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<EmptyResponse>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        passwordCommandUseCase.updatePassword(userDetails.getMemberId(), request.toCommand());

        return ApiResponse.success("비밀번호가 변경되었습니다", new EmptyResponse());
    }

    @PostMapping("/me/password/verify")
    @Operation(
            summary = "현재 비밀번호 검증",
            description = "비밀번호 변경이나 회원 탈퇴 없이, 로그인한 사용자의 현재 비밀번호 일치 여부만 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 검증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<EmptyResponse>> verifyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody VerifyPasswordRequest request
    ) {
        passwordCommandUseCase.verifyCurrentPassword(userDetails.getMemberId(), request.currentPassword());

        return ApiResponse.success("현재 비밀번호가 확인되었습니다.", new EmptyResponse());
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "내 프로필 수정",
            description = "로그인한 사용자의 프로필 이미지와 비밀번호를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 이미지 개수 초과"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<ProfileCommandUseCase.MyProfileUpdateView>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute UpdateMyProfileRequest request
    ) {
        if (request.hasMultipleProfileImages()) {
            throw new BusinessException(ErrorCode.PROFILE_IMAGE_COUNT_EXCEEDED);
        }

        return ApiResponse.success(
                "내 프로필이 수정되었습니다.",
                profileCommandUseCase.handle(new UpdateMyProfileCommand(
                        userDetails.getMemberId(),
                        request.getSingleProfileImage(),
                        request.currentPassword(),
                        request.newPassword(),
                        request.newPasswordConfirm()
                ))
        );
    }

    @PatchMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "프로필 이미지 수정",
            description = "로그인한 사용자의 프로필 이미지를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 이미지 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "파일 형식 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<ProfileImageResponse>> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        ProfileCommandUseCase.MyProfileUpdateView result = profileCommandUseCase.handle(
                new UpdateMyProfileCommand(
                        userDetails.getMemberId(),
                        profileImage,
                        null,
                        null,
                        null
                )
        );

        return ApiResponse.success(
                "프로필 이미지가 수정되었습니다.",
                new ProfileImageResponse(result.profileImageUrl())
        );
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 사용자의 현재 비밀번호를 확인한 뒤 회원 상태를 탈퇴로 변경하고 Refresh Token을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<EmptyResponse>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WithdrawMemberRequest request
    ) {
        profileCommandUseCase.withdraw(userDetails.getMemberId(), request.toCommand());

        return ApiResponse.success("회원 탈퇴가 완료되었습니다.", new EmptyResponse());
    }
}
