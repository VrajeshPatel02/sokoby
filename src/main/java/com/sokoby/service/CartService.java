package com.sokoby.service;

import com.sokoby.payload.CartDto;
import com.sokoby.payload.CartItemDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface CartService {
    CartDto getOrCreateCartByCustomerId(UUID customerId);

    CartDto addItemToCart(UUID customerId, CartItemDto itemDto);

    CartDto updateCartItem(UUID customerId, UUID cartItemId, CartItemDto itemDto);

    CartDto removeItemFromCart(UUID customerId, UUID cartItemId);

    void clearCart(UUID customerId);

    @Transactional(readOnly = true)
    @Cacheable(value = "carts", key = "#customerId")
    CartDto getCartByCustomerId(UUID customerId);
}