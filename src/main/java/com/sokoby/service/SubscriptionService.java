package com.sokoby.service;

import com.stripe.exception.StripeException;

import java.util.UUID;

public interface SubscriptionService {
    String createSubscription(UUID merchantId, boolean isAnnual) throws StripeException;
}