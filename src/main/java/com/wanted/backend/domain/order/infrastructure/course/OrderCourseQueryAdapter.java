package com.wanted.backend.domain.order.infrastructure.course;

import com.wanted.backend.domain.order.application.port.OrderCourseQueryPort;
import com.wanted.backend.global.config.S3UrlPresigner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderCourseQueryAdapter implements OrderCourseQueryPort {

    private final OrderCourseRefRepository courseRepository;
    private final S3UrlPresigner s3UrlPresigner;

    @Override
    @Transactional(readOnly = true)
    public List<CourseInfo> findAllByIds(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return courseRepository.findByIdIn(courseIds).stream()
                .filter(c -> "PUBLISHED".equals(c.getStatus()))
                .map(c -> new CourseInfo(
                        c.getId(),
                        c.getTitle(),
                        c.getPrice() != null ? c.getPrice() : 0,
                        s3UrlPresigner.publicUrl(c.getThumbnailUrl())))
                .toList();
    }
}
