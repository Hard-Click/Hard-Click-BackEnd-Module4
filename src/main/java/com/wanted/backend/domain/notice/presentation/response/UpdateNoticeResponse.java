package com.wanted.backend.domain.notice.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateNoticeResponse(

        @Schema(description = "수정된 공지사항 ID", example = "5")
        Long noticeId

) {

}