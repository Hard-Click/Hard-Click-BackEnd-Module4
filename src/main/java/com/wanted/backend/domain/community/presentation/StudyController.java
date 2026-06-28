package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.presentation.request.CreateStudyRequest;
import com.wanted.backend.domain.community.presentation.response.CreateStudyResponse;
import com.wanted.backend.domain.community.presentation.response.StudyDetailResponse;
import com.wanted.backend.domain.community.presentation.response.StudyListResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.domain.SubjectType;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "스터디 목록 조회", description = "스터디 모집 목록을 조회합니다. subject로 과목 필터링이 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스터디 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<StudyListResponse>> getStudyList(
            // SubjectType → String
            @Parameter(description = "과목 필터 (SubjectType enum 값, 예: MATH_1, ENG_1)", example = "MATH_1")
            @RequestParam(required = false) SubjectType subject,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 조회 수", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        List<StudyListResponse.StudyItem> content = List.of(
                new StudyListResponse.StudyItem(101L, "주말 React 스터디 모집", "강남 카페에서 진행합니다", "최*진", "React", 3, 6, false, LocalDateTime.of(2026, 5, 17, 14, 30)),
                new StudyListResponse.StudyItem(102L, "수학1 1등급 목표 스터디", "매주 일요일 밤 10시", "이*연", "수학1", 5, 5, true, LocalDateTime.of(2026, 5, 16, 10, 0))
        );

        return ApiResponse.success("스터디 목록 조회 완료", new StudyListResponse(content, 1));
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "스터디 상세 조회", description = "스터디 모집 글의 상세 내용을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스터디 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<StudyDetailResponse>> getStudyDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회할 스터디 그룹 ID", example = "101")
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
    @Operation(summary = "스터디 모집 등록", description = "스터디 모집 글을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스터디 모집 등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<CreateStudyResponse>> createStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateStudyRequest request) {

        return ApiResponse.created("스터디 모집글이 성공적으로 등록되었습니다.", new CreateStudyResponse(45L, 890L));
    }

    @PatchMapping("/{groupId}")
    @Operation(summary = "스터디 수정", description = "본인이 등록한 스터디 모집 글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스터디 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "본인이 등록한 스터디가 아님"),
            @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> updateStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "수정할 스터디 그룹 ID", example = "101")
            @PathVariable Long groupId,
            @Valid @RequestBody CreateStudyRequest request) {

        return ApiResponse.success("스터디가 수정되었습니다.", null);
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "스터디 삭제", description = "본인이 등록한 스터디 모집 글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "스터디 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "본인이 등록한 스터디가 아님"),
            @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> deleteStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "삭제할 스터디 그룹 ID", example = "101")
            @PathVariable Long groupId) {

        return ApiResponse.successNoContent("스터디가 삭제되었습니다.");
    }
}