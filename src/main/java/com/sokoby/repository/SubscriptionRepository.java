package com.sokoby.repository;

import com.sokoby.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByMerchantId(UUID merchantId);
    Optional<Subscription> findByStripeCheckoutSessionId(String sessionId);

    Optional<Subscription> findByStripeSubscriptionId(String subscriptionId);
}