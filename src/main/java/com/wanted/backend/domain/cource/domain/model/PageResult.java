package com.wanted.backend.domain.cource.domain.model;

import java.util.List;

/**
 * Spring 의존 없이 사용하는 페이지 결과 (순수 Java)
 */
public record PageResult<T>(
        List<T> content,
        int currentPage,
        int totalPages,
        long totalCount
) {}
