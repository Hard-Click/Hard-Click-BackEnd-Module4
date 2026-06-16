package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.application.command.CreateCourseOrderCommand;

import java.util.List;

public interface CreateCourseOrderUseCase {
    Result handle(CreateCourseOrderCommand command);

    record Result(Long orderId, String orderNo, List<Item> items, Integer totalAmount) {
        public record Item(Long courseId, String courseTitle, Integer price) {}
    }
}
