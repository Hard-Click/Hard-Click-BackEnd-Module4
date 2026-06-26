package com.wanted.backend.domain.order;

import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.model.OrderType;
import com.wanted.backend.domain.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderPersistenceIntegrationTest {

    @Autowired
    OrderRepository orderRepository;

    @Test
    void 코스주문_저장후_주문번호로_조회하면_항목까지_복원된다() {
        String orderNo = "ORD-TEST-" + System.nanoTime();
        Order order = Order.create(
                orderNo, 99999L, OrderType.COURSE, 89000, 89000, LocalDateTime.now(),
                List.of(OrderItem.create(123456789L, "테스트 강의", 89000)));

        Order saved = orderRepository.save(order);
        assertThat(saved.getId()).isNotNull(); // IDENTITY 자동생성 검증

        Order found = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.READY);
        assertThat(found.getType()).isEqualTo(OrderType.COURSE);
        assertThat(found.getTotalAmount()).isEqualTo(89000);
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getCourseId()).isEqualTo(123456789L);
        assertThat(found.getItems().get(0).getPrice()).isEqualTo(89000);
        assertThat(found.getItems().get(0).isRefunded()).isFalse();
    }

    @Test
    void 구독주문_저장시_항목없이_저장된다() {
        String orderNo = "ORD-TEST-SUB-" + System.nanoTime();
        Order order = Order.create(
                orderNo, 99999L, OrderType.SUBSCRIPTION, 1_580_000, 1_580_000, LocalDateTime.now(),
                List.of());

        Order saved = orderRepository.save(order);
        assertThat(saved.getId()).isNotNull();

        Order found = orderRepository.findByOrderNo(orderNo).orElseThrow();
        assertThat(found.getType()).isEqualTo(OrderType.SUBSCRIPTION);
        assertThat(found.getItems()).isEmpty();
    }
}
