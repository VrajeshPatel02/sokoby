package com.sokoby.service;

import com.sokoby.payload.PaymentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    PaymentDto createPayment(PaymentDto dto);

    PaymentDto getPaymentById(UUID id);

    PaymentDto getPaymentByOrderId(UUID orderId);

    List<PaymentDto> getPaymentsByCustomerId(UUID customerId);

    Page<PaymentDto> getPaymentsByCustomerId(UUID customerId, Pageable pageable);

    List<PaymentDto> getPaymentsByStoreId(UUID storeId);

    Page<PaymentDto> getPaymentsByStoreId(UUID storeId, Pageable pageable);

    PaymentDto updatePayment(UUID id, PaymentDto dto);

    void deletePayment(UUID id);

    @Transactional
    PaymentDto confirmPayment(String paymentIntentId);

    PaymentDto cancelPayment(String paymentIntentId);
}