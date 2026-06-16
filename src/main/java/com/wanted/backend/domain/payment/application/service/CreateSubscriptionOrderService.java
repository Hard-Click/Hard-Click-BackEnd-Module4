package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.command.CreateSubscriptionOrderCommand;
import com.wanted.backend.domain.payment.application.port.SubscriptionPlanQueryPort;
import com.wanted.backend.domain.payment.application.usecase.CreateSubscriptionOrderUseCase;
import com.wanted.backend.domain.payment.domain.model.Order;
import com.wanted.backend.domain.payment.domain.repository.OrderRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class CreateSubscriptionOrderService implements CreateSubscriptionOrderUseCase {

    private static final AtomicInteger sequence = new AtomicInteger(1);

    private final SubscriptionPlanQueryPort subscriptionPlanQueryPort;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Result handle(CreateSubscriptionOrderCommand command) {
        SubscriptionPlanQueryPort.PlanInfo plan = subscriptionPlanQueryPort.findById(command.planId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        String orderNo = generateOrderNo("SUB");
        Order order = Order.createSubscriptionOrder(command.memberId(), orderNo, command.planId());
        Order saved = orderRepository.save(order);

        return new Result(saved.getId(), orderNo, plan.name(), plan.price());
    }

    private String generateOrderNo(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return prefix + "-" + date + "-" + String.format("%03d", sequence.getAndIncrement());
    }
}
