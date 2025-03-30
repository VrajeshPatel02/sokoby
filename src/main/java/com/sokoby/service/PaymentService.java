package com.sokoby.service;

import com.sokoby.payload.PaymentDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    @Transactional
    String createPaymentSession(UUID orderId);

    PaymentDto createPayment(UUID orderId);
    PaymentDto getPaymentById(UUID id);
    PaymentDto getPaymentByOrderId(UUID orderId);

    PaymentDto confirmPayment(String id);

    @Transactional
    void handleFailedPayment(String paymentIntentId, String errorMessage, Map<String, String> metadata);
}