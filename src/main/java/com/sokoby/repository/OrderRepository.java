package com.sokoby.repository;

import com.sokoby.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    boolean existsByCustomerId(UUID customerId);

    boolean existsByStoreId(UUID storeId);

    List<Order> findByCustomerId(UUID customerId);

    List<Order> findByStoreId(UUID storeId);
}