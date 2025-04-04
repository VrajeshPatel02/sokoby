package com.sokoby.controller;

import com.sokoby.payload.CartDto;
import com.sokoby.payload.CartItemDto;
import com.sokoby.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CartDto> getCart(@PathVariable UUID customerId) {
        return ResponseEntity.ok(cartService.getOrCreateCartByCustomerId(customerId));
    }

    @PostMapping("/{customerId}/add")
    public ResponseEntity<CartDto> addItemToCart(
            @PathVariable UUID customerId,
            @RequestBody CartItemDto itemDto) {
        return ResponseEntity.ok(cartService.addItemToCart(customerId, itemDto));
    }

    @PutMapping("/{customerId}/update/{cartItemId}")
    public ResponseEntity<CartDto> updateCartItem(
            @PathVariable UUID customerId,
            @PathVariable UUID cartItemId,
            @RequestBody CartItemDto itemDto) {
        return ResponseEntity.ok(cartService.updateCartItem(customerId, cartItemId, itemDto));
    }

    @DeleteMapping("/{customerId}/remove/{cartItemId}")
    public ResponseEntity<CartDto> removeItemFromCart(
            @PathVariable UUID customerId,
            @PathVariable UUID cartItemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(customerId, cartItemId));
    }

    @DeleteMapping("/{customerId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable UUID customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}