package com.wanted.backend.domain.order.infrastructure.cart;

import com.wanted.backend.domain.order.application.port.OrderCartQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderCartQueryAdapter implements OrderCartQueryPort {

    private final OrderCartItemRefRepository cartItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Long> findCartCourseIds(Long memberId) {
        return cartItemRepository.findByMemberId(memberId).stream()
                .map(OrderCartItemRefEntity::getCourseId)
                .toList();
    }
}
