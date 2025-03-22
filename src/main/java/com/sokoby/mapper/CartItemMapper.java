package com.sokoby.mapper;

import com.sokoby.entity.CartItem;
import com.sokoby.payload.CartItemDto;

public class CartItemMapper {
    private CartItemMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static CartItemDto toDto(CartItem cartItem) {
        if (cartItem == null) {
            throw new IllegalArgumentException("CartItem entity cannot be null");
        }

        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setVariantId(cartItem.getVariant().getId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());
        return dto;
    }

    public static CartItem toEntity(CartItemDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CartItemDto cannot be null");
        }

        CartItem cartItem = new CartItem();
        cartItem.setId(dto.getId());
        cartItem.setQuantity(dto.getQuantity());
        // Cart and Variant relationships set in service
        return cartItem;
    }
}