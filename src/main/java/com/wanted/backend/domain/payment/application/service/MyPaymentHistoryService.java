package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.port.MyPaymentHistoryQueryPort;
import com.wanted.backend.domain.payment.application.port.PaymentCourseDisplayNamePort;
import com.wanted.backend.domain.payment.application.port.PaymentSubscriptionPlanDisplayNamePort;
import com.wanted.backend.domain.payment.application.usecase.GetMyPaymentHistoryUseCase;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPaymentHistoryService implements GetMyPaymentHistoryUseCase {

    private final MyPaymentHistoryQueryPort myPaymentHistoryQueryPort;
    private final PaymentCourseDisplayNamePort paymentCourseDisplayNamePort;
    private final PaymentSubscriptionPlanDisplayNamePort paymentSubscriptionPlanDisplayNamePort;

    @Override
    public List<MyPaymentHistoryView> handle(Long memberId) {
        List<MyPaymentHistoryQueryPort.MyPaymentHistoryData> histories =
                myPaymentHistoryQueryPort.findByMemberId(memberId);

        Map<Long, String> courseNameById = paymentCourseDisplayNamePort.findNamesByCourseIds(
                histories.stream()
                        .flatMap(history -> history.courseIds().stream())
                        .distinct()
                        .toList()
        );
        Map<Long, String> planNameById = paymentSubscriptionPlanDisplayNamePort.findNamesByPlanIds(
                histories.stream()
                        .map(MyPaymentHistoryQueryPort.MyPaymentHistoryData::subscriptionPlanId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        );

        return histories.stream()
                .map(history -> toView(history, courseNameById, planNameById))
                // 결제 내역 화면은 최신 결제일시가 위에 오도록 보여준다.
                .sorted(Comparator.comparing(
                        MyPaymentHistoryView::paidAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    // 결제/주문 조회 데이터와 별도 포트로 조회한 표시명을 합쳐 응답용 View로 변환한다.
    private MyPaymentHistoryView toView(
            MyPaymentHistoryQueryPort.MyPaymentHistoryData data,
            Map<Long, String> courseNameById,
            Map<Long, String> planNameById
    ) {
        return new MyPaymentHistoryView(
                data.paymentId(),
                data.orderId(),
                data.orderNo(),
                data.paymentType(),
                data.amount(),
                data.status(),
                data.paidAt(),
                displayName(data, courseNameById, planNameById)
        );
    }

    private String displayName(
            MyPaymentHistoryQueryPort.MyPaymentHistoryData data,
            Map<Long, String> courseNameById,
            Map<Long, String> planNameById
    ) {
        if (Objects.equals(data.paymentType(), PaymentType.SUBSCRIPTION)) {
            return planNameById.getOrDefault(data.subscriptionPlanId(), "(삭제된 구독 플랜)");
        }

        if (data.courseIds().isEmpty()) {
            return "(삭제된 강의)";
        }

        return data.courseIds().stream()
                .map(courseId -> courseNameById.getOrDefault(courseId, "(삭제된 강의)"))
                .distinct()
                .reduce((first, second) -> first + ", " + second)
                .orElse("(삭제된 강의)");
    }
}
