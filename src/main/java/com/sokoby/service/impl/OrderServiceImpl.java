package com.sokoby.service.impl;

import com.sokoby.entity.*;
import com.sokoby.enums.OrderStatus;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.AddressMapper;
import com.sokoby.payload.OrderDto;
import com.sokoby.repository.*;
import com.sokoby.mapper.OrderMapper;
import com.sokoby.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final VariantRepository variantRepository; // Added for inventory checks
    private final InventoryService inventoryService; // Added for inventory management

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, StoreRepository storeRepository,
                            CustomerRepository customerRepository, VariantRepository variantRepository,
                            InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.variantRepository = variantRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderDto createOrder(OrderDto dto) {
        validateOrderInput(dto);

        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new MerchantException("Customer not found", "CUSTOMER_NOT_FOUND"));

        Order order = OrderMapper.toEntity(dto);
        order.setStore(store);
        order.setCustomer(customer);
        order.setShippingAddress(AddressMapper.toEntity(dto.getShippingAddress()));

        // Add order items and check inventory
        dto.getOrderItems().forEach(itemDto -> {
            Variant variant = variantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
            if (!inventoryService.isAvailable(variant.getId(), itemDto.getQuantity())) {
                throw new MerchantException("Insufficient stock for variant: " + variant.getId(), "INSUFFICIENT_STOCK");
            }
            OrderItem item = new OrderItem();
            item.setVariant(variant);
            item.setQuantity(itemDto.getQuantity());
            order.addOrderItem(item);
        });

        // Reserve inventory
        order.getOrderItems().forEach(item -> inventoryService.reserveStock(item.getVariant().getId(), item.getQuantity()));

        try {
            Order savedOrder = orderRepository.save(order);
            logger.info("Created order {} for customer {}", savedOrder.getId(), customer.getId());
            return OrderMapper.toDto(savedOrder);
        } catch (Exception e) {
            logger.error("Failed to create order: {}", e.getMessage());
            throw new MerchantException("Failed to create order", "ORDER_CREATION_ERROR");
        }
    }

    @Override
    @Cacheable(value = "orders", key = "#id")
    public OrderDto getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));
        logger.info("Retrieved order with ID: {}", id);
        return OrderMapper.toDto(order);
    }

    @Override
    public List<OrderDto> getOrdersByCustomerId(UUID customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream().map(OrderMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersByStoreId(UUID storeId) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        return orders.stream().map(OrderMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderDto updateOrder(UUID id, OrderDto dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

        validateOrderInput(dto);
        validateStatusTransition(order.getStatus(), OrderStatus.valueOf(dto.getStatus()));

        // Update fields
        if (dto.getStoreId() != null) {
            order.setStore(storeRepository.findById(dto.getStoreId())
                    .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND")));
        }
        if (dto.getCustomerId() != null) {
            order.setCustomer(customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new MerchantException("Customer not found", "CUSTOMER_NOT_FOUND")));
        }
        if (dto.getShippingAddress() != null) {
            order.setShippingAddress(AddressMapper.toEntity(dto.getShippingAddress()));
        }
        if (dto.getStatus() != null) {
            order.setStatus(OrderStatus.valueOf(dto.getStatus()));
        }

        // Handle order items update
        order.getOrderItems().clear();
        dto.getOrderItems().forEach(itemDto -> {
            Variant variant = variantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
            OrderItem item = new OrderItem();
            item.setVariant(variant);
            item.setQuantity(itemDto.getQuantity());
            order.addOrderItem(item);
        });

        try {
            Order updatedOrder = orderRepository.save(order);
            logger.info("Updated order with ID: {}", id);
            return OrderMapper.toDto(updatedOrder);
        } catch (Exception e) {
            logger.error("Failed to update order with ID: {}", id, e);
            throw new MerchantException("Failed to update order", "ORDER_UPDATE_ERROR");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

        // Release reserved inventory if order is deleted
        order.getOrderItems().forEach(item ->
                inventoryService.releaseStock(item.getVariant().getId(), item.getQuantity()));

        try {
            orderRepository.deleteById(id);
            logger.info("Deleted order with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete order with ID: {}", id, e);
            throw new MerchantException("Failed to delete order", "ORDER_DELETION_ERROR");
        }
    }

    private void validateOrderInput(OrderDto dto) {
        if (dto.getStoreId() == null) throw new MerchantException("Store ID cannot be null", "INVALID_STORE_ID");
        if (dto.getCustomerId() == null) throw new MerchantException("Customer ID cannot be null", "INVALID_CUSTOMER_ID");
        if (dto.getShippingAddress() == null) throw new MerchantException("Shipping address cannot be null", "INVALID_SHIPPING_ADDRESS_ID");
        if (dto.getOrderItems() == null || dto.getOrderItems().isEmpty()) throw new MerchantException("Order must have at least one item", "INVALID_ORDER_ITEMS");
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // Example state machine logic
        if (current == OrderStatus.CANCELED && next != OrderStatus.CANCELED) {
            throw new MerchantException("Cannot change status from CANCELED", "INVALID_STATUS_TRANSITION");
        }
        if (current == OrderStatus.SHIPPED && next == OrderStatus.PLACED) {
            throw new MerchantException("Cannot revert SHIPPED to PLACED", "INVALID_STATUS_TRANSITION");
        }
    }

    // Other methods (getOrdersByCustomerId, getOrdersByStoreId) remain largely unchanged
}