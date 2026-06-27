package com.wanted.backend.domain.order.infrastructure.cart;

import com.wanted.backend.domain.order.application.port.OrderCartDeletePort;
import com.wanted.backend.domain.order.application.port.OrderCartQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderCartQueryAdapter implements OrderCartQueryPort, OrderCartDeletePort {

    private final OrderCartItemRefRepository cartItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Long> findCartCourseIds(Long memberId) {
        return cartItemRepository.findByMemberId(memberId).stream()
                .map(OrderCartItemRefEntity::getCourseId)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByMemberIdAndCourseIds(Long memberId, List<Long> courseIds) {
        if (!courseIds.isEmpty()) {
            cartItemRepository.deleteByMemberIdAndCourseIdIn(memberId, courseIds);
        }
    }

    @Override
    @Transactional
    public void deleteAllByMemberId(Long memberId) {
        cartItemRepository.deleteAllByMemberId(memberId);
    }
}
