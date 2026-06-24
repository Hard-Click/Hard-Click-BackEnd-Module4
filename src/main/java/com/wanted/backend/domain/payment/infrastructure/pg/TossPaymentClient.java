package com.wanted.backend.domain.payment.infrastructure.pg;

import com.wanted.backend.domain.payment.application.port.PgClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * Toss Payments 실제 결제 승인(confirm) API 연동.
 * prod 프로파일에서만 활성화되며, 시크릿키는 환경변수로만 주입받는다(코드/설정파일에 직접 기재 금지).
 */
@Slf4j
@Component
@Profile("prod")
public class TossPaymentClient implements PgClient {

    private final RestClient restClient;

    public TossPaymentClient(
            @Value("${toss.payments.base-url}") String baseUrl,
            @Value("${toss.payments.secret-key}") String secretKey
    ) {
        String credentials = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String confirm(String paymentKey, String orderId, Integer amount) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri("/v1/payments/confirm")
                    .body(Map.of(
                            "paymentKey", paymentKey,
                            "orderId", orderId,
                            "amount", amount
                    ))
                    .retrieve()
                    .body(Map.class);

            // Toss confirm 응답은 요청값을 echo한다 — 클라이언트 조작/오류로 인한
            // 금액·주문번호·결제건 불일치를 여기서 반드시 걸러내야 한다.
            String confirmedPaymentKey = Objects.toString(response != null ? response.get("paymentKey") : null, null);
            String confirmedOrderId = Objects.toString(response != null ? response.get("orderId") : null, null);
            Object totalAmount = response != null ? response.get("totalAmount") : null;

            if (confirmedPaymentKey == null) {
                throw new TossPaymentException("Toss confirm 응답에 paymentKey가 없습니다. orderId=" + orderId);
            }
            if (!confirmedPaymentKey.equals(paymentKey)) {
                throw new TossPaymentException("응답 paymentKey가 요청값과 다릅니다. orderId=" + orderId);
            }
            if (!Objects.equals(confirmedOrderId, orderId)) {
                throw new TossPaymentException("응답 orderId가 요청값과 다릅니다. orderId=" + orderId);
            }
            if (totalAmount == null || !String.valueOf(totalAmount).equals(String.valueOf(amount))) {
                throw new TossPaymentException("응답 금액이 요청 금액과 다릅니다. orderId=" + orderId);
            }
            return confirmedPaymentKey;
        } catch (RestClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            log.error("Toss confirm 실패 (orderId={}, status={}): {}", orderId, status, e.getResponseBodyAsString());
            throw new TossPaymentException("Toss confirm 실패: " + e.getResponseBodyAsString(), e);
        }
    }

    @Override
    public void cancel(String paymentKey, Integer cancelAmount, String cancelReason) {
        try {
            restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(Map.of(
                            "cancelReason", cancelReason,
                            "cancelAmount", cancelAmount
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            log.error("Toss cancel 실패 (paymentKey={}, status={}): {}", paymentKey, status, e.getResponseBodyAsString());
            throw new TossPaymentException("Toss cancel 실패: " + e.getResponseBodyAsString(), e);
        }
    }

    public static class TossPaymentException extends RuntimeException {
        public TossPaymentException(String message) {
            super(message);
        }

        public TossPaymentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
