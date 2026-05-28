package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.command.UpdateMyProfileCommand;
import com.wanted.backend.domain.identity.application.usecase.GetMyProfileUseCase;
import com.wanted.backend.domain.identity.application.usecase.UpdateMyProfileUseCase;
import com.wanted.backend.domain.identity.application.usecase.UpdatePasswordUseCase;
import com.wanted.backend.domain.identity.application.usecase.WithdrawMemberUseCase;
import com.wanted.backend.domain.identity.presentation.api.request.UpdateMyProfileRequest;
import com.wanted.backend.domain.identity.presentation.api.request.UpdatePasswordRequest;
import com.wanted.backend.domain.identity.presentation.api.request.WithdrawMemberRequest;
import com.wanted.backend.domain.identity.presentation.api.response.EmptyResponse;
import com.wanted.backend.domain.identity.presentation.api.response.ProfileImageResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "User Profile", description = "마이페이지 프로필 API")
public class UserProfileController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final WithdrawMemberUseCase withdrawMemberUseCase;

    @GetMapping("/me")
    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 사용자의 아이디, 이름, 이메일, 프로필 이미지를 조회합니다."
    )
    public ResponseEntity<ApiResponse<GetMyProfileUseCase.MyProfileView>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                "내 프로필이 조회되었습니다.",
                getMyProfileUseCase.handle(userDetails.getMemberId())
        );
    }
    @PatchMapping("/me/password")
    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        updatePasswordUseCase.updatePassword(userDetails.getMemberId(), request.toCommand());

        return ApiResponse.success("비밀번호가 변경되었습니다.", null);
    }



    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "내 프로필 수정",
            description = "로그인한 사용자의 프로필 이미지와 비밀번호를 수정합니다."
    )
    public ResponseEntity<ApiResponse<UpdateMyProfileUseCase.MyProfileUpdateView>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute UpdateMyProfileRequest request
    ) {
        if (request.hasMultipleProfileImages()) {
            throw new BusinessException(ErrorCode.PROFILE_IMAGE_COUNT_EXCEEDED);
        }

        return ApiResponse.success(
                "내 프로필이 수정되었습니다.",
                updateMyProfileUseCase.handle(new UpdateMyProfileCommand(
                        userDetails.getMemberId(),
                        request.getSingleProfileImage(),
                        request.getCurrentPassword(),
                        request.getNewPassword(),
                        request.getNewPasswordConfirm()
                ))

        );

    }
    @PatchMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "프로필 이미지 수정",
            description = "로그인한 사용자의 프로필 이미지를 수정합니다."
    )
    public ResponseEntity<ApiResponse<EmptyResponse>> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        UpdateMyProfileUseCase.MyProfileUpdateView result = updateMyProfileUseCase.handle(
                new UpdateMyProfileCommand(
                        userDetails.getMemberId(),
                        profileImage,
                        null,
                        null,
                        null
                )
        );

        return ApiResponse.success("비밀번호가 변경되었습니다.", new EmptyResponse());

    }
    @DeleteMapping("/me")
    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 사용자의 현재 비밀번호를 확인한 뒤 회원 상태를 탈퇴로 변경하고 Refresh Token을 삭제합니다."
    )
    public ResponseEntity<ApiResponse<EmptyResponse>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WithdrawMemberRequest request
    ) {
        withdrawMemberUseCase.withdraw(userDetails.getMemberId(), request.toCommand());

        return ApiResponse.success("회원 탈퇴가 완료되었습니다", new EmptyResponse());
    }
}
