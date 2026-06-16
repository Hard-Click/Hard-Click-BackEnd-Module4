package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.CourseForOrderQueryPort;
import com.wanted.backend.domain.payment.application.port.OrderItemCreatePort;
import com.wanted.backend.domain.payment.infrastructure.course.PaymentCourseReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderItemCreateAdapter implements OrderItemCreatePort {

    private final WritableOrderItemJpaRepository repository;
    private final PaymentCourseReferenceRepository courseRepository;

    @Override
    public void saveAll(Long orderId, List<Long> courseIds) {
        Map<Long, com.wanted.backend.domain.payment.infrastructure.course.PaymentCourseReferenceEntity> courseMap =
                courseRepository.findByIdIn(courseIds).stream()
                        .collect(Collectors.toMap(e -> e.getId(), e -> e));

        List<WritableOrderItemJpaEntity> items = courseIds.stream()
                .map(courseId -> {
                    var course = courseMap.get(courseId);
                    String title = course != null ? course.getTitle() : "";
                    Integer price = course != null ? course.getPrice() : 0;
                    return WritableOrderItemJpaEntity.create(orderId, courseId, title, price);
                })
                .toList();

        repository.saveAll(items);
    }
}
