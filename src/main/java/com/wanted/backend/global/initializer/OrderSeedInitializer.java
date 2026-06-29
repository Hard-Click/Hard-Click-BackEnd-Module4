package com.wanted.backend.global.initializer;

import com.wanted.backend.domain.enrollment_management.application.command.EnrollCommand;
import com.wanted.backend.domain.enrollment_management.application.usecase.EnrollUseCase;
import com.wanted.backend.domain.identity.infrastructure.persistence.MemberJpaEntity;
import com.wanted.backend.domain.identity.infrastructure.persistence.MemberJpaRepository;
import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.model.OrderType;
import com.wanted.backend.domain.order.domain.repository.OrderRepository;
import com.wanted.backend.domain.order.infrastructure.course.OrderCourseRefEntity;
import com.wanted.backend.domain.order.infrastructure.course.OrderCourseRefRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 환불 E2E 테스트용 PAID 주문 시드.
 * 서버 기동 시 student1 계정에 PAID+수강등록 상태의 주문이 없으면 1회 생성한다.
 * local/dev/default 프로파일에서만 활성화.
 */
@Slf4j
@Component
@Profile({"local", "dev", "default"})
@RequiredArgsConstructor
public class OrderSeedInitializer {

    private static final String SEED_ORDER_NO = "ORD-20260512-SEED0001";

    private final MemberJpaRepository memberJpaRepository;
    private final OrderCourseRefRepository courseRefRepository;
    private final OrderRepository orderRepository;
    private final EnrollUseCase enrollUseCase;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        try {
            doSeed();
        } catch (Exception e) {
            log.warn("[SEED] 환불 테스트 주문 시드 실패 (서버 기동은 계속): {}", e.getMessage());
        }
    }

    private void doSeed() {
        if (orderRepository.findByOrderNo(SEED_ORDER_NO).isPresent()) {
            return;
        }

        MemberJpaEntity student1 = memberJpaRepository.findByUsername("student1").orElse(null);
        if (student1 == null) {
            log.warn("[SEED] student1 미존재 → 환불 테스트 주문 시드 스킵");
            return;
        }

        List<OrderCourseRefEntity> courses = courseRefRepository.findAll(PageRequest.of(0, 2)).getContent();
        if (courses.isEmpty()) {
            log.warn("[SEED] 강의 데이터 없음 → 환불 테스트 주문 시드 스킵");
            return;
        }

        LocalDateTime orderedAt = LocalDateTime.of(2026, 5, 12, 14, 30);
        LocalDateTime paidAt    = LocalDateTime.of(2026, 5, 12, 14, 31);

        List<OrderItem> items = courses.stream()
                .map(c -> OrderItem.create(
                        c.getId(),
                        c.getTitle() != null ? c.getTitle() : "강의",
                        c.getPrice() != null ? c.getPrice() : 0))
                .toList();
        int total = items.stream().mapToInt(OrderItem::getPrice).sum();

        Order order = Order.restore(null, SEED_ORDER_NO, student1.getId(),
                OrderType.COURSE, OrderStatus.PAID,
                total, total, orderedAt, paidAt, null, items);
        orderRepository.save(order);
        orderRepository.markPaid(SEED_ORDER_NO, paidAt, "seed-payment-key-001");

        for (OrderItem item : items) {
            try {
                enrollUseCase.handle(new EnrollCommand(student1.getId(), item.getCourseId()));
            } catch (Exception e) {
                log.debug("[SEED] 수강 등록 스킵 courseId={}: {}", item.getCourseId(), e.getMessage());
            }
        }

        log.info("[SEED] 환불 테스트용 PAID 주문 생성 완료 orderNo={} memberId={} totalAmount={}",
                SEED_ORDER_NO, student1.getId(), total);
    }
}
