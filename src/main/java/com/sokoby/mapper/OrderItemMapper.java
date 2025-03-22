package com.sokoby.mapper;

import com.sokoby.entity.OrderItem;
import com.sokoby.payload.OrderItemDto;

public class OrderItemMapper {
    private OrderItemMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static OrderItemDto toDto(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("OrderItem entity cannot be null");
        }

        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setOrderId(orderItem.getOrder().getId());
        dto.setVariantId(orderItem.getVariant().getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getSubtotal());
        dto.setCreatedAt(orderItem.getCreatedAt());
        dto.setUpdatedAt(orderItem.getUpdatedAt());
        return dto;
    }

    public static OrderItem toEntity(OrderItemDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("OrderItemDto cannot be null");
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setId(dto.getId());
        orderItem.setQuantity(dto.getQuantity());
        orderItem.setPrice(dto.getPrice());
        // Order and Variant relationships set in service
        return orderItem;
    }
}