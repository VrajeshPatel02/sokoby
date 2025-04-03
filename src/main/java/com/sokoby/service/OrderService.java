package com.sokoby.service;

import com.sokoby.payload.OrderDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(OrderDto dto);

    OrderDto getOrderById(UUID id);

    List<OrderDto> getOrdersByCustomerId(UUID customerId);

    List<OrderDto> getOrdersByStoreId(UUID storeId);

    OrderDto updateOrder(UUID id, OrderDto dto);

    void deleteOrder(UUID id);

    OrderDto updateOrderStatus(UUID id, String status);

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    OrderDto createOrderWithCustomerDetails(OrderDto dto);
}