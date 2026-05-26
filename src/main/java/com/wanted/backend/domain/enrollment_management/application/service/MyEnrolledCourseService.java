package com.wanted.backend.domain.enrollment_management.application.service;

import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrolledCourseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyEnrolledCourseService implements GetMyEnrolledCourseUseCase {


    @Override
    public List<MyEnrolledCourseView> handle(Long memberId) {
        return List.of();
    }
}
