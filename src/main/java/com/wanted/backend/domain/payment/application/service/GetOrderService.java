package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.port.OrderQueryPort;
import com.wanted.backend.domain.payment.application.usecase.GetOrderUseCase;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetOrderService implements GetOrderUseCase {

    private final OrderQueryPort orderQueryPort;

    @Override
    @Transactional(readOnly = true)
    public Result handle(Long orderId, Long memberId) {
        OrderQueryPort.OrderData data = orderQueryPort.findById(orderId);

        List<Result.Item> items = data.items().stream()
                .map(i -> new Result.Item(i.courseId(), i.courseTitle(), i.price()))
                .toList();

        int totalAmount = data.items().stream().mapToInt(OrderQueryPort.OrderItemData::price).sum();

        return new Result(data.orderId(), data.orderNo(), data.paymentType(), items, totalAmount);
    }
}
