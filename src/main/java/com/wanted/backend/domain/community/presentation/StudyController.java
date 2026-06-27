package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.presentation.request.CreateStudyRequest;
import com.wanted.backend.domain.community.presentation.response.CreateStudyResponse;
import com.wanted.backend.domain.community.presentation.response.StudyDetailResponse;
import com.wanted.backend.domain.community.presentation.response.StudyListResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.domain.SubjectType;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Study", description = "스터디 모집 API")
@RestController
@RequestMapping("/api/studies")
public class StudyController {

    @GetMapping
    public ResponseEntity<ApiResponse<StudyListResponse>> getStudyList(
            // SubjectType → String
            @RequestParam(required = false) SubjectType subject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<StudyListResponse.StudyItem> content = List.of(
                new StudyListResponse.StudyItem(101L, "주말 React 스터디 모집", "강남 카페에서 진행합니다", "최*진", "React", 3, 6, false, LocalDateTime.of(2026, 5, 17, 14, 30)),
                new StudyListResponse.StudyItem(102L, "수학1 1등급 목표 스터디", "매주 일요일 밤 10시", "이*연", "수학1", 5, 5, true, LocalDateTime.of(2026, 5, 16, 10, 0))
        );

        return ApiResponse.success("스터디 목록 조회 완료", new StudyListResponse(content, 1));
    }

    @Operation(summary = "스터디 목록 조회", description = "스터디 목록을 조회합니다. subject로 과목 필터링 가능 (SubjectType enum 값)")
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<StudyDetailResponse>> getStudyDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {

        boolean isJoined = true;

        StudyDetailResponse response = new StudyDetailResponse(
                groupId,
                "수학 1등급 목표 스터디",
                "매주 일요일 밤 10시에 모여서 질문 받습니다.",
                "수학1", "이*연", 2, 5, false, isJoined, false,
                isJoined ? List.of("이*연", "김*민") : null,
                LocalDateTime.of(2026, 5, 18, 17, 0)
        );

        return ApiResponse.success("스터디 상세 조회 완료", response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateStudyResponse>> createStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateStudyRequest request) {

        return ApiResponse.created("스터디 모집글이 성공적으로 등록되었습니다.", new CreateStudyResponse(45L, 890L));
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> updateStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateStudyRequest request) {

        return ApiResponse.success("스터디가 수정되었습니다.", null);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {

        return ApiResponse.successNoContent("스터디가 삭제되었습니다.");
    }
}