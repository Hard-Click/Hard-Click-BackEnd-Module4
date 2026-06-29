package com.wanted.backend.domain.identity.presentation.api.response;

import com.wanted.backend.domain.identity.application.dto.ChangeMemberStatusResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 상태 변경 응답")
public record ChangeMemberStatusResponse(
        @Schema(description = "상태가 변경된 회원 ID", example = "1")
        Long memberId,
        @Schema(description = "변경된 회원 상태 (ACTIVE, SUSPENDED)", example = "SUSPENDED")
        String status,
        @Schema(description = "관리자 메모", example = "신고 누적 검토 후 커뮤니티 이용을 제한합니다.")
        String memo
) {
    public static ChangeMemberStatusResponse from(ChangeMemberStatusResult result) {
        return new ChangeMemberStatusResponse(
                result.memberId(),
                result.status().name(),
                result.memo()
        );
    }
}
