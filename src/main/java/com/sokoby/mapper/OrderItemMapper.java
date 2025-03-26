package com.sokoby.mapper;

import com.sokoby.entity.OrderItem;
import com.sokoby.payload.OrderItemDto;

public class OrderItemMapper {
    private OrderItemMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static OrderItemDto toDto(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("OrderItem entity cannot be null for mapping to DTO");
        }

        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setOrderId(orderItem.getOrder() != null ? orderItem.getOrder().getId() : null);
        dto.setVariantId(orderItem.getVariant() != null ? orderItem.getVariant().getId() : null);
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getSubtotal());
        dto.setCreatedAt(orderItem.getCreatedAt());
        dto.setUpdatedAt(orderItem.getUpdatedAt());
        return dto;
    }

    public static OrderItem toEntity(OrderItemDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("OrderItemDto cannot be null for mapping to entity");
        }

        OrderItem orderItem = new OrderItem();
        if (dto.getId() != null) {
            orderItem.setId(dto.getId());
        }
        orderItem.setQuantity(dto.getQuantity());
        // Note: price and subtotal are set in @PrePersist based on variant
        // Order and Variant relationships are set in the service layer
        return orderItem;
    }
}