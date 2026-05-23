package com.wanted.backend.domain.cource.presentation.api;

import com.wanted.backend.domain.cource.application.command.ChangeCourseStatusCommand;
import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;
import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.application.query.CourseListQuery;
import com.wanted.backend.domain.cource.application.usecase.*;
import com.wanted.backend.domain.cource.domain.model.CourseSortType;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.FileProcessingStatus;
import com.wanted.backend.domain.cource.presentation.api.request.CreateCourseRequest;
import com.wanted.backend.domain.cource.presentation.api.request.UpdateCourseRequest;
import com.wanted.backend.domain.cource.presentation.api.response.CourseListResponse;
import com.wanted.backend.domain.cource.presentation.api.response.CreateCourseResponse;
import com.wanted.backend.domain.cource.presentation.api.response.UploadLessonVideoResponse;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final GetCourseListUseCase getCourseListUseCase;
    private final CreateCourseUseCase createCourseUseCase;
    private final UpdateCourseUseCase updateCourseUseCase;
    private final DeleteCourseUseCase deleteCourseUseCase;
    private final ChangeCourseStatusUseCase changeCourseStatusUseCase;
    private final UploadLessonVideoUseCase uploadLessonVideoUseCase;

    /**
     * 강의 목록 페이징 조회
     * GET /api/courses?keyword=&subject=&instructorName=&sort=LATEST&page=0&size=12
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CourseListResponse>> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String instructorName,
            @RequestParam(defaultValue = "LATEST") CourseSortType sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        CourseListResult result = getCourseListUseCase.handle(
                new CourseListQuery(keyword, subject, instructorName, sort, page, size));
        return ApiResponse.success("강의 목록 조회 성공", CourseListResponse.from(result));
    }

    /**
     * 강의 등록
     * POST /api/courses
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCourseResponse>> createCourse(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody CreateCourseRequest request
    ) {
        Long courseId = createCourseUseCase.handle(request.toCommand(memberId));
        return ApiResponse.created("강의가 등록되었습니다.", new CreateCourseResponse(courseId));
    }

    /**
     * 강의 수정
     * PATCH /api/courses/{courseId}
     */
    @PatchMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> updateCourse(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long courseId,
            @Valid @RequestBody UpdateCourseRequest request
    ) {
        updateCourseUseCase.handle(request.toCommand(courseId, memberId));
        return ApiResponse.successNoContent("강의가 수정되었습니다.");
    }

    /**
     * 강의 삭제
     * DELETE /api/courses/{courseId}
     */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long courseId
    ) {
        deleteCourseUseCase.handle(courseId, memberId);
        return ApiResponse.successNoContent("강의가 삭제되었습니다.");
    }

    /**
     * 강의 공개/비공개 처리
     * PATCH /api/courses/{courseId}/status
     * body: { "published": true | false }
     */
    @PatchMapping("/{courseId}/status")
    public ResponseEntity<ApiResponse<Void>> changeCourseStatus(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long courseId,
            @RequestParam boolean published
    ) {
        CourseStatus targetStatus = published ? CourseStatus.PUBLISHED : CourseStatus.DRAFT;
        changeCourseStatusUseCase.handle(new ChangeCourseStatusCommand(courseId, memberId, targetStatus));
        String message = published ? "강의가 공개되었습니다." : "강의가 비공개 처리되었습니다.";
        return ApiResponse.successNoContent(message);
    }

    /**
     * 회차 영상 업로드
     * POST /api/courses/lessons/{lessonId}/video
     */
    @PostMapping(value = "/lessons/{lessonId}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadLessonVideoResponse>> uploadLessonVideo(
            @PathVariable Long lessonId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String videoUrl = uploadLessonVideoUseCase.handle(
                new UploadLessonVideoCommand(lessonId, file.getOriginalFilename(), file.getBytes())
        );
        return ApiResponse.success("영상이 업로드되었습니다.",
                new UploadLessonVideoResponse(lessonId, videoUrl, FileProcessingStatus.PENDING));
    }
}
