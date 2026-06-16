package com.wanted.backend.domain.payment.infrastructure.toss;

import com.wanted.backend.domain.payment.application.service.ConfirmPaymentService;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

@Component
public class TossPaymentClientImpl implements ConfirmPaymentService.TossPaymentClient {

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    @Value("${toss.secret-key:test_sk_placeholder}")
    private String secretKey;

    private final RestClient restClient = RestClient.create();

    @Override
    public Response confirm(String paymentKey, String orderId, Integer amount) {
        String encoded = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.post()
                    .uri(TOSS_CONFIRM_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                    .retrieve()
                    .body(Map.class);

            String approvedAtStr = body != null ? (String) body.get("approvedAt") : null;
            LocalDateTime approvedAt = approvedAtStr != null
                    ? OffsetDateTime.parse(approvedAtStr).toLocalDateTime()
                    : LocalDateTime.now();

            return new Response(approvedAt);
        } catch (HttpClientErrorException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
