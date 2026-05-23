package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.application.port.InstructorQueryPort;
import com.wanted.backend.domain.cource.application.query.CourseListQuery;
import com.wanted.backend.domain.cource.application.usecase.GetCourseListUseCase;
import com.wanted.backend.domain.cource.domain.model.CourseListItem;
import com.wanted.backend.domain.cource.domain.model.PageResult;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCourseListService implements GetCourseListUseCase {

    private final CourseRepository courseRepository;
    private final InstructorQueryPort instructorQueryPort;

    @Override
    public CourseListResult handle(CourseListQuery query) {
        // 강사명 필터가 있을 경우 먼저 authorId 목록 조회
        List<Long> authorIds = null;
        if (query.instructorName() != null && !query.instructorName().isBlank()) {
            authorIds = instructorQueryPort.findIdsByName(query.instructorName());
            if (authorIds.isEmpty()) {
                // 해당 강사가 없으면 빈 결과 반환
                return new CourseListResult(Collections.emptyList(), query.page(), 0, 0);
            }
        }

        PageResult<CourseListItem> pageResult = courseRepository.findList(
                query.keyword(), query.subject(), authorIds, query.sort(), query.page(), query.size());

        // authorId → 강사명 일괄 조회 (N+1 방지)
        List<Long> ids = pageResult.content().stream()
                .map(CourseListItem::authorId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> nameMap = ids.isEmpty() ? Map.of() : instructorQueryPort.findNamesByIds(ids);

        List<CourseListResult.Item> items = pageResult.content().stream()
                .map(item -> new CourseListResult.Item(
                        item.courseId(),
                        item.title(),
                        item.subject(),
                        item.thumbnailUrl(),
                        item.priceType(),
                        item.price(),
                        nameMap.getOrDefault(item.authorId(), "알 수 없음"),
                        0.0,   // 추후 리뷰 도메인 연동
                        0,     // 추후 리뷰 도메인 연동
                        0      // 추후 수강신청 도메인 연동
                ))
                .collect(Collectors.toList());

        return new CourseListResult(items, pageResult.currentPage(),
                pageResult.totalPages(), pageResult.totalCount());
    }
}
