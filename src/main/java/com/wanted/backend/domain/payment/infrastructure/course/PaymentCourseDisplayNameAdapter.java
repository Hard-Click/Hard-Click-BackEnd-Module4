package com.wanted.backend.domain.payment.infrastructure.course;

import com.wanted.backend.domain.payment.application.port.PaymentCourseDisplayNamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentCourseDisplayNameAdapter implements PaymentCourseDisplayNamePort {

    private final PaymentCourseReferenceRepository courseRepository;

    @Override
    public Map<Long, String> findNamesByCourseIds(Collection<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }

        return courseRepository.findByIdIn(courseIds).stream()
                .collect(Collectors.toMap(PaymentCourseReferenceEntity::getId, PaymentCourseReferenceEntity::getTitle));
    }
}
