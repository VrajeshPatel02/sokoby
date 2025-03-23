package com.sokoby.service;

import com.sokoby.payload.OrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(OrderDto dto);

    OrderDto getOrderById(UUID id);

    List<OrderDto> getOrdersByCustomerId(UUID customerId);

    Page<OrderDto> getOrdersByCustomerId(UUID customerId, Pageable pageable);

    List<OrderDto> getOrdersByStoreId(UUID storeId);

    Page<OrderDto> getOrdersByStoreId(UUID storeId, Pageable pageable);

    OrderDto updateOrder(UUID id, OrderDto dto);

    void deleteOrder(UUID id);
}