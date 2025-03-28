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
            throw new IllegalArgumentException("Payment entity cannot be null for mapping to DTO");
        }

        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStripePaymentIntentId(payment.getStripePaymentIntentId());
        dto.setStatus(payment.getStatus().toString());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setStripeCheckoutSessionId(payment.getStripeCheckoutSessionId());
        return dto;
    }

    public static Payment toEntity(PaymentDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("PaymentDto cannot be null for mapping to entity");
        }

        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setAmount(dto.getAmount());
        payment.setCurrency(dto.getCurrency());
        payment.setStripePaymentIntentId(dto.getStripePaymentIntentId());
        payment.setStatus(PaymentStatus.valueOf(dto.getStatus()));
        payment.setStripeCheckoutSessionId(dto.getStripeCheckoutSessionId());
        return payment;
    }
}