package com.sokoby.service;

import com.sokoby.payload.PaymentDto;

import java.util.UUID;

public interface PaymentService {
    PaymentDto createPayment(UUID orderId, String paymentMethodId);
    PaymentDto getPaymentById(UUID id);
    PaymentDto getPaymentByOrderId(UUID orderId);
}