package com.sokoby.service;

import com.sokoby.payload.PaymentDto;
import com.sokoby.payload.SubscriptionDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    SubscriptionDto createSubscriptionSession(SubscriptionDto subscriptionDto);

    SubscriptionDto updateSubscription(UUID subscriptionId, SubscriptionDto subscriptionDto);

    void cancelSubscription(UUID subscriptionId, boolean immediate);

    SubscriptionDto getSubscriptionById(UUID id);

    List<SubscriptionDto> getAllSubscriptions();

    SubscriptionDto renewSubscription(UUID subscriptionId);

    void retrySubscriptionPayment(UUID subscriptionId);
}