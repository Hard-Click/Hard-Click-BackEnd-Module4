package com.wanted.backend.domain.cource.presentation.api;

import com.wanted.backend.domain.cource.application.command.ChangeCourseStatusCommand;
import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;
import com.wanted.backend.domain.cource.application.dto.CourseDetailResult;
import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.application.query.CourseListQuery;
import com.wanted.backend.domain.cource.application.usecase.*;
import com.wanted.backend.domain.cource.domain.model.CourseSortType;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.FileProcessingStatus;
import com.wanted.backend.domain.cource.presentation.api.request.CreateCourseRequest;
import com.wanted.backend.domain.cource.presentation.api.request.UpdateCourseRequest;
import com.wanted.backend.domain.cource.presentation.api.response.CourseDetailResponse;
import com.wanted.backend.domain.cource.presentation.api.response.CourseListResponse;
import com.wanted.backend.domain.cource.presentation.api.response.CreateCourseResponse;
import com.wanted.backend.domain.cource.presentation.api.response.UploadLessonVideoResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Course", description = "강의 목록/상세 조회, 등록/수정/삭제, 영상 업로드 API")
public class CourseController {

    private final GetCourseListUseCase getCourseListUseCase;
    private final CourseQueryUseCase courseQueryUseCase;
    private final CreateCourseUseCase createCourseUseCase;
    private final UpdateCourseUseCase updateCourseUseCase;
    private final DeleteCourseUseCase deleteCourseUseCase;
    private final ChangeCourseStatusUseCase changeCourseStatusUseCase;
    private final UploadLessonVideoUseCase uploadLessonVideoUseCase;

    @GetMapping
    @Operation(summary = "강의 목록 조회", description = "키워드/과목/강사명 필터와 정렬을 적용하여 강의 목록을 페이징 조회합니다.")
    public ResponseEntity<ApiResponse<CourseListResponse>> getCourses(
            @Parameter(description = "검색 키워드 (강의명)", example = "수학") @RequestParam(required = false) String keyword,
            @Parameter(description = "과목명 필터", example = "수학Ⅱ") @RequestParam(required = false) String subject,
            @Parameter(description = "강사명 필터", example = "박지훈") @RequestParam(required = false) String instructorName,
            @Parameter(description = "정렬 기준 (LATEST/POPULAR/RATING)", example = "LATEST") @RequestParam(defaultValue = "LATEST") CourseSortType sort,
            @Parameter(description = "페이지 번호 (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수", example = "12") @RequestParam(defaultValue = "12") int size
    ) {
        CourseListResult result = getCourseListUseCase.handle(
                new CourseListQuery(keyword, subject, instructorName, sort, page, size));
        return ApiResponse.success("강의 목록 조회 성공", CourseListResponse.from(result));
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "강의 상세 조회", description = "강의 상세 정보와 커리큘럼, 미리보기 여부를 반환합니다. 비로그인 시 isEnrolled 는 false 처리됩니다.")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(
            @Parameter(description = "강의 ID", example = "1") @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        CourseDetailResult result = courseQueryUseCase.getDetail(courseId, memberId);
        return ApiResponse.success("강의 상세 조회 성공", CourseDetailResponse.from(result));
    }

    @PostMapping
    @Operation(summary = "강의 등록", description = "강사가 커리큘럼(섹션/레슨) 포함 신규 강의를 등록합니다. INSTRUCTOR 권한 필요.")
    public ResponseEntity<ApiResponse<CreateCourseResponse>> createCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCourseRequest request
    ) {
        Long courseId = createCourseUseCase.handle(request.toCommand(userDetails.getMemberId()));
        return ApiResponse.created("강의가 등록되었습니다.", new CreateCourseResponse(courseId));
    }

    @PatchMapping("/{courseId}")
    @Operation(summary = "강의 수정", description = "강사가 본인 강의 정보를 수정합니다. INSTRUCTOR 권한 필요.")
    public ResponseEntity<ApiResponse<Void>> updateCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "강의 ID", example = "1") @PathVariable Long courseId,
            @Valid @RequestBody UpdateCourseRequest request
    ) {
        updateCourseUseCase.handle(request.toCommand(courseId, userDetails.getMemberId()));
        return ApiResponse.successNoContent("강의가 수정되었습니다.");
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "강의 삭제", description = "강사가 본인 강의를 삭제합니다. INSTRUCTOR 권한 필요.")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "강의 ID", example = "1") @PathVariable Long courseId
    ) {
        deleteCourseUseCase.handle(courseId, userDetails.getMemberId());
        return ApiResponse.successNoContent("강의가 삭제되었습니다.");
    }

    @PatchMapping("/{courseId}/status")
    @Operation(summary = "강의 공개/비공개 처리", description = "강사가 본인 강의를 공개(PUBLISHED) 또는 비공개(DRAFT)로 변경합니다. INSTRUCTOR 권한 필요.")
    public ResponseEntity<ApiResponse<Void>> changeCourseStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "강의 ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "true = 공개, false = 비공개", example = "true") @RequestParam boolean published
    ) {
        CourseStatus targetStatus = published ? CourseStatus.PUBLISHED : CourseStatus.DRAFT;
        changeCourseStatusUseCase.handle(new ChangeCourseStatusCommand(courseId, userDetails.getMemberId(), targetStatus));
        String message = published ? "강의가 공개되었습니다." : "강의가 비공개 처리되었습니다.";
        return ApiResponse.successNoContent(message);
    }

    @PostMapping(value = "/lessons/{lessonId}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "레슨 영상 업로드", description = "레슨 ID에 해당하는 영상 파일을 업로드합니다. multipart/form-data 형식으로 전송.")
    public ResponseEntity<ApiResponse<UploadLessonVideoResponse>> uploadLessonVideo(
            @Parameter(description = "레슨 ID", example = "3") @PathVariable Long lessonId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String videoUrl = uploadLessonVideoUseCase.handle(
                new UploadLessonVideoCommand(lessonId, file.getOriginalFilename(), file.getBytes())
        );
        return ApiResponse.success("영상이 업로드되었습니다.",
                new UploadLessonVideoResponse(lessonId, videoUrl, FileProcessingStatus.PENDING));
    }
}
