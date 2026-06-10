package com.wanted.backend.domain.subject.presentation.api;

import com.wanted.backend.domain.subject.application.usecase.GetSubjectListUseCase;
import com.wanted.backend.domain.subject.presentation.api.response.SubjectResponse;
import com.wanted.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final GetSubjectListUseCase getSubjectListUseCase;

    /**
     * 과목 목록 조회
     * GET /api/subjects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjects() {
        List<SubjectResponse> response = getSubjectListUseCase.handle().stream()
                .map(SubjectResponse::from)
                .toList();
        return ApiResponse.success("과목 목록 조회 성공", response);
    }
}
