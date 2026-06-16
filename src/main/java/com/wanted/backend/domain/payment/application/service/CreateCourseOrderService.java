package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.command.CreateCourseOrderCommand;
import com.wanted.backend.domain.payment.application.port.CourseForOrderQueryPort;
import com.wanted.backend.domain.payment.application.port.OrderItemCreatePort;
import com.wanted.backend.domain.payment.application.usecase.CreateCourseOrderUseCase;
import com.wanted.backend.domain.payment.domain.model.Order;
import com.wanted.backend.domain.payment.domain.model.OrderItem;
import com.wanted.backend.domain.payment.domain.repository.OrderRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateCourseOrderService implements CreateCourseOrderUseCase {

    private static final AtomicInteger sequence = new AtomicInteger(1);

    private final CourseForOrderQueryPort courseForOrderQueryPort;
    private final OrderRepository orderRepository;
    private final OrderItemCreatePort orderItemCreatePort;

    @Override
    @Transactional
    public Result handle(CreateCourseOrderCommand command) {
        if (command.courseIds() == null || command.courseIds().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<CourseForOrderQueryPort.CourseInfo> courses =
                courseForOrderQueryPort.findAllByIds(command.courseIds());

        if (courses.size() != command.courseIds().size()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        Map<Long, CourseForOrderQueryPort.CourseInfo> courseMap = courses.stream()
                .collect(Collectors.toMap(CourseForOrderQueryPort.CourseInfo::courseId, c -> c));

        List<OrderItem> items = command.courseIds().stream()
                .map(courseId -> {
                    CourseForOrderQueryPort.CourseInfo info = courseMap.get(courseId);
                    return OrderItem.create(courseId, info.title(), info.price());
                })
                .toList();

        String orderNo = generateOrderNo("ORD");
        Order order = Order.createCourseOrder(command.memberId(), orderNo, items);
        Order saved = orderRepository.save(order);

        orderItemCreatePort.saveAll(saved.getId(), command.courseIds());

        int totalAmount = items.stream().mapToInt(OrderItem::getPrice).sum();

        List<Result.Item> resultItems = items.stream()
                .map(i -> new Result.Item(i.getCourseId(), i.getCourseTitle(), i.getPrice()))
                .toList();

        return new Result(saved.getId(), orderNo, resultItems, totalAmount);
    }

    private String generateOrderNo(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return prefix + "-" + date + "-" + String.format("%03d", sequence.getAndIncrement());
    }
}
