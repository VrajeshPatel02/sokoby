package com.sokoby.service;

import com.sokoby.payload.OrderItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderItemService {
    OrderItemDto createOrderItem(OrderItemDto dto);

    OrderItemDto getOrderItemById(UUID id);

    List<OrderItemDto> getOrderItemsByOrderId(UUID orderId);

    Page<OrderItemDto> getOrderItemsByOrderId(UUID orderId, Pageable pageable);

    OrderItemDto updateOrderItem(UUID id, OrderItemDto dto);

    void deleteOrderItem(UUID id);
}