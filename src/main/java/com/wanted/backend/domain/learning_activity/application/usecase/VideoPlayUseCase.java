package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import io.swagger.v3.oas.annotations.media.Schema;

// 외부에 제공하는 계약
// 컨트롤러는 구현체인 VideoPlayService 몰라도 됨 유스케이스만 바라본다
public interface VideoPlayUseCase {

    // 해당 API 가 반환할 응답 모델
    VideoPlayView handle(MemberVideoCommand command);

    @Schema(description = "영상 재생 정보 응답")
    record VideoPlayView(
            @Schema(description = "영상 ID (lessonId와 동일)", example = "1")
            Long videoId,

            @Schema(description = "강의 ID", example = "20")
            Long courseId,

            @Schema(description = "S3 Presigned 스트리밍 URL (7일 만료)",
                    example = "https://hard-click-bucket.s3.ap-northeast-2.amazonaws.com/videos/1_abc.mp4?X-Amz-Algorithm=...")
            String streamingUrl,

            @Schema(description = "영상 전체 재생시간 (초)", example = "3600")
            Integer durationSeconds,

            @Schema(description = "마지막 재생 위치 (초, 이어보기용)", example = "320")
            Integer lastPositionSec,

            @Schema(description = "누적 시청 시간 (초)", example = "1800")
            Integer watchTimeSec,

            @Schema(description = "영상 완료 여부", example = "false")
            Boolean completed
    ) {
    }
}
