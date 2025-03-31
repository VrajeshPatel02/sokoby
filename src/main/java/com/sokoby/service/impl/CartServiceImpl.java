package com.sokoby.service.impl;

import com.sokoby.entity.Cart;
import com.sokoby.entity.CartItem;
import com.sokoby.entity.Customer;
import com.sokoby.entity.Variant;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.CartItemMapper;
import com.sokoby.mapper.CartMapper;
import com.sokoby.payload.CartDto;
import com.sokoby.payload.CartItemDto;
import com.sokoby.repository.CartItemRepository;
import com.sokoby.repository.CartRepository;
import com.sokoby.repository.CustomerRepository;
import com.sokoby.repository.VariantRepository;
import com.sokoby.service.CartService;
import com.sokoby.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final VariantRepository variantRepository;
    private final InventoryService inventoryService;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                           CustomerRepository customerRepository, VariantRepository variantRepository, InventoryService inventoryService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.variantRepository = variantRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    @Cacheable(value = "carts", key = "#customerId")
    public CartDto getOrCreateCartByCustomerId(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new MerchantException("Customer not found", "CUSTOMER_NOT_FOUND"));

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewCart(customer));

        logger.info("Retrieved or created cart for customer ID: {}", customerId);
        return CartMapper.toDtoWithItems(cart);
    }

    @Transactional
    private Cart createNewCart(Customer customer) {
        Cart cart = new Cart();
        cart.setCustomer(customer);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    @CacheEvict(value = "carts", key = "#customerId")
    public CartDto addItemToCart(UUID customerId, CartItemDto itemDto) {
        validateCartItemInput(itemDto);

        Cart cart = getOrCreateCart(customerId);
        Variant variant = variantRepository.findById(itemDto.getVariantId())
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));

        // Update to use InventoryService for stock check
        if (!inventoryService.isAvailable(variant.getId(), itemDto.getQuantity())) {
            throw new MerchantException("Insufficient stock for variant " + variant.getName(), "INSUFFICIENT_STOCK");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId());
        CartItem cartItem;

        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + itemDto.getQuantity());
        } else {
            cartItem = CartItemMapper.toEntity(itemDto);
            cartItem.setCart(cart);
            cartItem.setVariant(variant);
        }
        try {
            cartItemRepository.save(cartItem);

            logger.info("Added item to cart for customer ID: {}", customerId);
            return CartMapper.toDto(cart);
        } catch (Exception e) {
            logger.error("Failed to add item to cart for customer {}: {}", customerId, e.getMessage());
            throw new MerchantException("Failed to add item to cart", "CART_ITEM_ADD_ERROR");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "carts", key = "#customerId")
    public CartDto updateCartItem(UUID customerId, UUID cartItemId, CartItemDto itemDto) {

        validateCartItemInput(itemDto);

        Cart cart = getOrCreateCart(customerId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new MerchantException("Cart item not found", "CART_ITEM_NOT_FOUND"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new MerchantException("Cart item does not belong to this cart", "INVALID_CART_ITEM");
        }

        Variant variant = variantRepository.findById(itemDto.getVariantId())
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));

        if (variant.getInventoryItem().getStockQuantity() < itemDto.getQuantity()) {
            throw new MerchantException("Insufficient stock for variant " + variant.getName(), "INSUFFICIENT_STOCK");
        }

        cartItem.setVariant(variant);
        cartItem.setQuantity(itemDto.getQuantity());

        try {
            cartItemRepository.save(cartItem);
            cartRepository.save(cart);
            logger.info("Updated cart item {} for customer ID: {}", cartItemId, customerId);
            return CartMapper.toDtoWithItems(cart);
        } catch (Exception e) {
            logger.error("Failed to update cart item {} for customer {}: {}", cartItemId, customerId, e.getMessage());
            throw new MerchantException("Failed to update cart item", "CART_ITEM_UPDATE_ERROR");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "carts", key = "#customerId")
    public CartDto removeItemFromCart(UUID customerId, UUID cartItemId) {
        Cart cart = getOrCreateCart(customerId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new MerchantException("Cart item not found", "CART_ITEM_NOT_FOUND"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new MerchantException("Cart item does not belong to this cart", "INVALID_CART_ITEM");
        }

        try {
            // First, remove the item from the cart's collection if using bidirectional relationship
            if (cart.getCartItems() != null) {
                cart.getCartItems().remove(cartItem);
            }

            // Then delete from the repository
            cartItemRepository.delete(cartItem);

            // Get fresh cart data from database to ensure consistency
            Cart updatedCart = cartRepository.findById(cart.getId())
                    .orElseThrow(() -> new MerchantException("Cart not found", "CART_NOT_FOUND"));

            logger.info("Removed cart item {} from cart for customer ID: {}", cartItemId, customerId);
            return CartMapper.toDtoWithItems(updatedCart);
        } catch (Exception e) {
            logger.error("Failed to remove cart item {} for customer {}: {}", cartItemId, customerId, e.getMessage());
            throw new MerchantException("Failed to remove cart item", "CART_ITEM_REMOVE_ERROR");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "carts", key = "#customerId")
    public void clearCart(UUID customerId) {
        Cart cart = getOrCreateCart(customerId);

        try {
            cartItemRepository.deleteAll(cart.getCartItems());
            // Clear the cart's collection to maintain consistency
            if (cart.getCartItems() != null) {
                cart.getCartItems().clear();
            }
            cartRepository.save(cart);
            logger.info("Cleared cart for customer ID: {}", customerId);
        } catch (Exception e) {
            logger.error("Failed to clear cart for customer {}: {}", customerId, e.getMessage());
            throw new MerchantException("Failed to clear cart", "CART_CLEAR_ERROR");
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "carts", key = "#customerId")
    @Override
    public CartDto getCartByCustomerId(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new MerchantException("Customer not found", "CUSTOMER_NOT_FOUND"));

        Optional<Cart> cartOptional = cartRepository.findByCustomerId(customerId);
        Cart cart;
        if (cartOptional.isEmpty()) {
            logger.info("No cart found for customer ID: {}, returning empty cart", customerId);
            cart = new Cart();
            cart.setCustomer(customer);
        } else {
            cart = cartOptional.get();
            // Force initialization of cartItems within transaction
            cart.getCartItems().size(); // This triggers loading of lazy-loaded items
        }

        return CartMapper.toDtoWithItems(cart);
    }

    private Cart getOrCreateCart(UUID customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewCart(customerRepository.findById(customerId)
                        .orElseThrow(() -> new MerchantException("Customer not found", "CUSTOMER_NOT_FOUND"))));
    }

    private void validateCartItemInput(CartItemDto dto) {
        if (dto.getVariantId() == null) {
            throw new MerchantException("Variant ID cannot be null", "INVALID_VARIANT_ID");
        }
        if (dto.getQuantity() == null || dto.getQuantity() < 1) {
            throw new MerchantException("Quantity must be at least 1", "INVALID_QUANTITY");
        }
    }
}