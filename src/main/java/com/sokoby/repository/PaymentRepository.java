package com.sokoby.repository;

import com.sokoby.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
  Optional<Payment> findByOrderId(UUID orderId);

  Optional<Payment> findByStripeCheckoutSessionId(String id);

    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);

    Optional<Payment> findByStripeSubscriptionId(String subscriptionId);
}