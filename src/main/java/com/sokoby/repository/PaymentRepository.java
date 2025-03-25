package com.sokoby.repository;

import com.sokoby.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  Optional<Payment> findByOrderId(UUID orderId);

  List<Payment> findByOrder_CustomerId(UUID customerId);

  Page<Payment> findByOrder_CustomerId(UUID customerId, Pageable pageable);

  List<Payment> findByOrder_StoreId(UUID storeId);

  Page<Payment> findByOrder_StoreId(UUID storeId, Pageable pageable);

  Optional<Payment> findByStripePaymentId(String paymentIntentId);
}