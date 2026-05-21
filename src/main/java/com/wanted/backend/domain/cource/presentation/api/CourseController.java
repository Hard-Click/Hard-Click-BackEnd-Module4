package com.wanted.backend.domain.cource.presentation.api;

import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;
import com.wanted.backend.domain.cource.application.usecase.CreateCourseUseCase;
import com.wanted.backend.domain.cource.application.usecase.UploadLessonVideoUseCase;
import com.wanted.backend.domain.cource.domain.model.FileProcessingStatus;
import com.wanted.backend.domain.cource.presentation.api.request.CreateCourseRequest;
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
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CreateCourseUseCase createCourseUseCase;
    private final UploadLessonVideoUseCase uploadLessonVideoUseCase;

    /**
     * 강의 등록
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
     * 회차 영상 업로드
     * POST /api/v1/courses/lessons/{lessonId}/video
     */
    @PostMapping(value = "/lessons/{lessonId}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadLessonVideoResponse>> uploadLessonVideo(
            @PathVariable Long lessonId,
            @RequestPart("file") MultipartFile file

    ) throws IOException {
        // IOException 자바 문법상 파일 읽기 에러(IOException)가 터질 수 있으니
        // 내가 직접 처리 안 하고 스프링 글로벌 핸들러한테 에러를 넘겨버리겠다(토스)는 선언
        String videoUrl = uploadLessonVideoUseCase.handle(
                new UploadLessonVideoCommand(lessonId, file.getOriginalFilename(), file.getBytes())
        );
        return ApiResponse.success("영상이 업로드되었습니다.",
                new UploadLessonVideoResponse(lessonId, videoUrl, FileProcessingStatus.PENDING));
    }
}
