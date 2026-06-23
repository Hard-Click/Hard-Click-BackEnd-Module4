package com.wanted.backend.domain.cart.application.service;

import com.wanted.backend.domain.cart.application.port.CartCourseQueryPort;
import com.wanted.backend.domain.cart.application.usecase.GetCartUseCase;
import com.wanted.backend.domain.cart.domain.model.CartItem;
import com.wanted.backend.domain.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCartService implements GetCartUseCase {

    private final CartRepository cartRepository;
    private final CartCourseQueryPort cartCourseQueryPort;

    @Override
    @Transactional(readOnly = true)
    public Result handle(Long memberId) {
        List<CartItem> cartItems = cartRepository.findAllByMemberId(memberId);

        if (cartItems.isEmpty()) {
            return new Result(List.of(), 0, 0);
        }

        List<Long> courseIds = cartItems.stream().map(CartItem::getCourseId).toList();
        Map<Long, CartCourseQueryPort.CourseDetail> courseMap = cartCourseQueryPort.findAllByIds(courseIds)
                .stream().collect(Collectors.toMap(CartCourseQueryPort.CourseDetail::courseId, d -> d));

        List<Item> items = cartItems.stream()
                .map(ci -> {
                    CartCourseQueryPort.CourseDetail detail = courseMap.get(ci.getCourseId());
                    return new Item(
                            ci.getCourseId(),
                            detail != null ? detail.title() : "",
                            detail != null ? detail.instructorName() : "",
                            detail != null ? detail.price() : 0
                    );
                })
                .toList();

        int total = items.stream().mapToInt(Item::price).sum();
        return new Result(items, items.size(), total);
    }
}
