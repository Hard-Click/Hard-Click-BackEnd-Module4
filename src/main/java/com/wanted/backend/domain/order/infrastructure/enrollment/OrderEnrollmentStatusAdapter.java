package com.wanted.backend.domain.order.infrastructure.enrollment;

import com.wanted.backend.domain.order.application.port.OrderEnrollmentStatusPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderEnrollmentStatusAdapter implements OrderEnrollmentStatusPort {

    private final OrderEnrollmentRefRepository enrollmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<Long, String> findEnrollStatuses(Long memberId, List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return enrollmentRepository.findByMemberAndCourseIds(memberId, courseIds).stream()
                .collect(Collectors.toMap(
                        OrderEnrollmentRefEntity::getCourseId,
                        OrderEnrollmentRefEntity::getStatus,
                        (a, b) -> a
                ));
    }
}
