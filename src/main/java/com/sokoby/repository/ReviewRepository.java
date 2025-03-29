package com.sokoby.repository;

import com.sokoby.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
  List<Review> findByProductId(UUID productId);
  List<Review> findByVariantId(UUID variantId);
  List<Review> findByCustomerId(UUID customerId);
  boolean existsByOrderIdAndProductId(UUID orderId, UUID productId);
  boolean existsByOrderIdAndVariantId(UUID orderId, UUID variantId);
}