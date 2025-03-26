package com.sokoby.mapper;

import com.sokoby.entity.Order;
import com.sokoby.enums.OrderStatus;
import com.sokoby.payload.OrderDto;

import java.util.stream.Collectors;

public class OrderMapper {
    private OrderMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static OrderDto toDto(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order entity cannot be null for mapping to DTO");
        }

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setStoreId(order.getStore() != null ? order.getStore().getId() : null);
        dto.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        dto.setShippingAddress(AddressMapper.toDto(order.getShippingAddress()));
        dto.setOrderItems(order.getOrderItems().stream()
                .map(OrderItemMapper::toDto)
                .collect(Collectors.toList()));
        dto.setSubtotal(order.getSubtotal());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscountCode(order.getDiscount() != null ? order.getDiscount().getCode() : null);
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }

    public static Order toEntity(OrderDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("OrderDto cannot be null for mapping to entity");
        }

        Order order = new Order();
        if (dto.getId() != null) {
            order.setId(dto.getId());
        }
        order.setSubtotal(dto.getSubtotal() != null ? dto.getSubtotal() : 0.0);
        order.setDiscountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : 0.0);
        order.setTotalAmount(dto.getTotalAmount() != null ? dto.getTotalAmount() : 0.0);
        order.setStatus(dto.getStatus() != null ? OrderStatus.valueOf(dto.getStatus()) : OrderStatus.PLACED);
        order.setShippingAddress(AddressMapper.toEntity(dto.getShippingAddress()));
        // Relationships (store, customer, orderItems, discount) set in service
        return order;
    }
}