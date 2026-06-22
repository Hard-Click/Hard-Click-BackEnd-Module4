package com.wanted.backend.domain.identity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeMemberStatusRequest(
        @Schema(description = "변경할 회원 상태", example = "SUSPENDED", allowableValues = {"ACTIVE", "SUSPENDED"})
        @NotBlank(message = "변경할 회원 상태는 필수입니다.")
        String status,

        @Schema(description = "상태 변경 사유 또는 관리자 메모", example = "신고 누적 검토 후 커뮤니티 이용을 제한합니다.")
        @Size(max = 50, message = "관리자 메모는 50자 이하여야 합니다.")
        String memo
) {
}
