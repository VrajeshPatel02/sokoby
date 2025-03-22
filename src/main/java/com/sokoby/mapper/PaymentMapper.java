package com.sokoby.mapper;

import com.sokoby.entity.Payment;
import com.sokoby.enums.PaymentStatus;
import com.sokoby.payload.PaymentDto;

public class PaymentMapper {
    private PaymentMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static PaymentDto toDto(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment entity cannot be null");
        }

        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().name());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }

    public static Payment toEntity(PaymentDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("PaymentDto cannot be null");
        }

        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setAmount(dto.getAmount());
        payment.setStatus(PaymentStatus.valueOf(dto.getStatus()));
        payment.setTransactionId(dto.getTransactionId());
        // Order relationship set in service
        return payment;
    }
}