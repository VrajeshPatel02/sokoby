package com.sokoby.service;

import com.sokoby.payload.PaymentDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface PaymentService {
    @Transactional
    String createPaymentSession(UUID orderId);

    PaymentDto createPayment(UUID orderId, String paymentMethodId);
    PaymentDto getPaymentById(UUID id);
    PaymentDto getPaymentByOrderId(UUID orderId);

    void confirmPayment(String id);
}