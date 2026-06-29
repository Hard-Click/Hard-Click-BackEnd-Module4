package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface GetVideoProgressUseCase {

    VideoProgressView handle(MemberVideoCommand command);

    @Schema(description = "단일 영상 진도 응답")
    record VideoProgressView(
            @Schema(description = "영상 ID (lessonId와 동일)", example = "10")
            Long videoId,

            @Schema(description = "마지막 재생 위치 (초)", example = "320")
            Integer lastPositionSeconds,

            @Schema(description = "누적 시청 시간 (초)", example = "1800")
            Integer watchTimeSeconds,

            @Schema(description = "영상 전체 재생시간 (초)", example = "3600")
            Integer durationSeconds,

            @Schema(description = "시청 진도율 (%)", example = "50.00")
            BigDecimal progressRate,

            @Schema(description = "영상 완료 여부", example = "false")
            Boolean completed,

            @Schema(description = "완료 처리 일시 (완료되지 않은 경우 null)", example = "2026-06-28T15:00:00")
            LocalDateTime completedAt
    ) {
    }
}
