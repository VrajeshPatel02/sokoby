package com.sokoby.mapper;

import com.sokoby.entity.Order;
import com.sokoby.enums.OrderStatus;
import com.sokoby.payload.OrderDto;

public class OrderMapper {
    private OrderMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static OrderDto toDto(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order entity cannot be null");
        }

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setStoreId(order.getStore().getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setShippingAddress(AddressMapper.toDto(order.getShippingAddress()));
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }

    public static Order toEntity(OrderDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("OrderDto cannot be null");
        }

        Order order = new Order();
        order.setId(dto.getId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setStatus(OrderStatus.valueOf(dto.getStatus()));
        // Relationships (store, customer, addresses, shipping method) set in service
        return order;
    }
}