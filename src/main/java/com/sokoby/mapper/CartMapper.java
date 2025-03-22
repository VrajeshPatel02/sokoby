package com.sokoby.mapper;

import com.sokoby.entity.Cart;
import com.sokoby.payload.CartDto;

import java.util.stream.Collectors;

public class CartMapper {
    private CartMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static CartDto toDto(Cart cart) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart entity cannot be null");
        }

        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setCustomerId(cart.getCustomer().getId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        return dto;
    }

    public static Cart toEntity(CartDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CartDto cannot be null");
        }

        Cart cart = new Cart();
        cart.setId(dto.getId());
        // Customer relationship set in service
        return cart;
    }

    public static CartDto toDtoWithItems(Cart cart) {
        CartDto dto = toDto(cart);
        dto.setCartItems(cart.getCartItems().stream()
                .map(CartItemMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }
}