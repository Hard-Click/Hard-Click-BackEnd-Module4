package com.wanted.backend.domain.cource.domain.repository;

import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.model.CourseListItem;
import com.wanted.backend.domain.cource.domain.model.CourseSortType;
import com.wanted.backend.domain.cource.domain.model.PageResult;

import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    Course save(Course course);
    Optional<Course> findById(Long courseId);
    void delete(Long courseId);

    /**
     * 공개 강의 목록 페이징 조회
     * @param keyword       강의명 검색어 (null = 전체)
     * @param subject       과목 필터 (null = 전체)
     * @param authorIds     강사 ID 목록 (null = 전체)
     * @param sort          정렬 기준
     * @param page          페이지 번호 (0-based)
     * @param size          페이지 크기
     */
    PageResult<CourseListItem> findList(String keyword, String subject, List<Long> authorIds,
                                       CourseSortType sort, int page, int size);
}
