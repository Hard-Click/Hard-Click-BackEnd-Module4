package com.wanted.backend.domain.community.presentation.request;

import com.wanted.backend.global.domain.SubjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스터디 모집 등록/수정 요청")
public record CreateStudyRequest(
        @Schema(description = "스터디 제목", example = "주말 수학 스터디 모집")
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @Schema(description = "과목 (SubjectType enum 값)", example = "MATH_1")
        @NotNull(message = "과목은 필수입니다.")
        SubjectType subject,

        @Schema(description = "최대 참여 인원 (최소 2명 이상)", example = "6")
        @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
        int maxCount,

        @Schema(description = "스터디 내용", example = "매주 토요일 오후 2시에 진행합니다.")
        @NotBlank(message = "내용은 필수입니다.")
        String content
) {}