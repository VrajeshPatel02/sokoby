package com.sokoby.service.impl;

import com.sokoby.entity.*;
import com.sokoby.enums.OrderStatus;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.AddressMapper;
import com.sokoby.payload.OrderDto;
import com.sokoby.payload.PaymentDto;
import com.sokoby.repository.*;
import com.sokoby.mapper.OrderMapper;
import com.sokoby.service.InventoryService;
import com.sokoby.service.OrderService;
import com.sokoby.service.PaymentService;
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
    private final VariantRepository variantRepository;
    private final InventoryService inventoryService;
    private final DiscountRepository discountRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final ProductRepository productRepository;


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, StoreRepository storeRepository,
                            CustomerRepository customerRepository, VariantRepository variantRepository,
                            InventoryService inventoryService, DiscountRepository discountRepository,
                            PaymentRepository paymentRepository, PaymentService paymentService,
                            ProductRepository productRepository){
        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.variantRepository = variantRepository;
        this.inventoryService = inventoryService;
        this.discountRepository = discountRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.productRepository = productRepository;
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

//        if (store.getStripeAccountId() == null) {
//            throw new MerchantException("Store has no payment gateway configured", "NO_PAYMENT_GATEWAY");
//        }

        Order order = new Order();
        order.setStore(store);
        order.setCustomer(customer);
        order.setShippingAddress(AddressMapper.toEntity(dto.getShippingAddress()));
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        dto.getOrderItems().forEach(itemDto -> {
            OrderItem item = new OrderItem();
            item.setQuantity(itemDto.getQuantity());

            if (itemDto.getProductId() != null && itemDto.getVariantId() == null) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
                if (!inventoryService.isAvailableForProduct(product.getId(), itemDto.getQuantity())) {
                    throw new MerchantException("Insufficient stock for product: " + product.getId(), "INSUFFICIENT_STOCK");
                }
                item.setProduct(product);
            } else if (itemDto.getVariantId() != null && itemDto.getProductId() == null) {
                Variant variant = variantRepository.findById(itemDto.getVariantId())
                        .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
                if (!inventoryService.isAvailable(variant.getId(), itemDto.getQuantity())) {
                    throw new MerchantException("Insufficient stock for variant: " + variant.getId(), "INSUFFICIENT_STOCK");
                }
                item.setVariant(variant);
            } else {
                throw new MerchantException("Order item must specify exactly one of productId or variantId", "INVALID_ORDER_ITEM");
            }

            order.addOrderItem(item);
        });

        if (dto.getDiscountCode() != null) {
            Discount discount = discountRepository.findByCode(dto.getDiscountCode())
                    .orElseThrow(() -> new MerchantException("Invalid discount code", "INVALID_DISCOUNT_CODE"));
            order.setDiscount(discount);
        }

        try {
            order.calculateTotals();
            Order savedOrder = orderRepository.save(order);

            // Create payment session
            PaymentDto paymentDto = paymentService.createPayment(savedOrder.getId());

            logger.info("Created order {} with Stripe Checkout Session for customer {} in store {}",
                    savedOrder.getId(), customer.getId(), store.getId());

            OrderDto orderDto = OrderMapper.toDto(savedOrder);
//            orderDto.setPaymentUrl(paymentDto.getStripeCheckoutUrl()); // Return the Stripe session URL to the frontend
            orderDto.setPaymentId(paymentDto.getId());


            return orderDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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

        if (dto.getStatus() != null) {
            try {
                OrderStatus nextStatus = OrderStatus.valueOf(dto.getStatus().toUpperCase());
                validateStatusTransition(order.getStatus(), nextStatus);
                order.setStatus(nextStatus);
            } catch (IllegalArgumentException e) {
                throw new MerchantException("Invalid order status: " + dto.getStatus(), "INVALID_STATUS");
            }
        }

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

        order.getOrderItems().clear();
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

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderDto updateOrderStatus(UUID id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

        if (status == null || status.trim().isEmpty()) {
            throw new MerchantException("Status cannot be null or empty", "INVALID_STATUS");
        }

        try {
            OrderStatus nextStatus = OrderStatus.valueOf(status.toUpperCase());
            validateStatusTransition(order.getStatus(), nextStatus);
            order.setStatus(nextStatus);
        } catch (IllegalArgumentException e) {
            throw new MerchantException("Invalid order status: " + status, "INVALID_STATUS");
        }

        try {
            Order updatedOrder = orderRepository.save(order);
            logger.info("Updated status of order {} to {}", id, status);
            return OrderMapper.toDto(updatedOrder);
        } catch (Exception e) {
            logger.error("Failed to update status of order {}: {}", id, e.getMessage());
            throw new MerchantException("Failed to update order status", "ORDER_STATUS_UPDATE_ERROR");
        }
    }

    private void validateOrderInput(OrderDto dto) {
        if (dto.getStoreId() == null) throw new MerchantException("Store ID cannot be null", "INVALID_STORE_ID");
        if (dto.getCustomerId() == null) throw new MerchantException("Customer ID cannot be null", "INVALID_CUSTOMER_ID");
        if (dto.getShippingAddress() == null) throw new MerchantException("Shipping address cannot be null", "INVALID_SHIPPING_ADDRESS_ID");
        if (dto.getOrderItems() == null || dto.getOrderItems().isEmpty()) throw new MerchantException("Order must have at least one item", "INVALID_ORDER_ITEMS");
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current.equals(OrderStatus.CANCELED) && !next.equals(OrderStatus.CANCELED)) {
            throw new MerchantException("Cannot change status from CANCELED to " + next, "INVALID_STATUS_TRANSITION");
        }
        if (current.equals(OrderStatus.SHIPPED) && next.equals(OrderStatus.PLACED)) {
            throw new MerchantException("Cannot revert SHIPPED to PLACED", "INVALID_STATUS_TRANSITION");
        }
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    @Override
    public OrderDto createOrderWithCustomerDetails(OrderDto dto) {
        validateOrderInputWithCustomerDetails(dto);

        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));

//        if (store.getStripeAccountId() == null) {
//            throw new MerchantException("Store has no payment gateway configured", "NO_PAYMENT_GATEWAY");
//        }

        Order order = new Order();
        order.setStore(store);
        order.setCustomerEmail(dto.getCustomerEmail());
        order.setCustomerFirstName(dto.getCustomerFirstName());
        order.setCustomerLastName(dto.getCustomerLastName());
        order.setCustomerPhoneNumber(dto.getCustomerPhoneNumber());
        order.setShippingAddress(AddressMapper.toEntity(dto.getShippingAddress()));
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        dto.getOrderItems().forEach(itemDto -> {
            OrderItem item = new OrderItem();
            item.setQuantity(itemDto.getQuantity());

            if (itemDto.getProductId() != null && itemDto.getVariantId() == null) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
                if (!inventoryService.isAvailableForProduct(product.getId(), itemDto.getQuantity())) {
                    throw new MerchantException("Insufficient stock for product: " + product.getId(), "INSUFFICIENT_STOCK");
                }
                item.setProduct(product);
            } else if (itemDto.getVariantId() != null && itemDto.getProductId() == null) {
                Variant variant = variantRepository.findById(itemDto.getVariantId())
                        .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
                if (!inventoryService.isAvailable(variant.getId(), itemDto.getQuantity())) {
                    throw new MerchantException("Insufficient stock for variant: " + variant.getId(), "INSUFFICIENT_STOCK");
                }
                item.setVariant(variant);
            } else {
                throw new MerchantException("Order item must specify exactly one of productId or variantId", "INVALID_ORDER_ITEM");
            }

            order.addOrderItem(item);
        });

        if (dto.getDiscountCode() != null) {
            Discount discount = discountRepository.findByCode(dto.getDiscountCode())
                    .orElseThrow(() -> new MerchantException("Invalid discount code", "INVALID_DISCOUNT_CODE"));
            order.setDiscount(discount);
        }

        try {
            order.calculateTotals();
            Order savedOrder = orderRepository.save(order);

            // Create payment session
            PaymentDto paymentDto = paymentService.createPayment(savedOrder.getId());

            OrderDto orderDto = OrderMapper.toDto(savedOrder);
            orderDto.setPaymentId(paymentDto.getId());
            return orderDto;
        } catch (Exception e) {
            logger.error("Failed to create order: {}", e.getMessage());
            throw new RuntimeException("Order creation failed", e);
        }
    }

    // Updated validation method for the new service method
    private void validateOrderInputWithCustomerDetails(OrderDto dto) {
        if (dto.getStoreId() == null) throw new MerchantException("Store ID cannot be null", "INVALID_STORE_ID");
        if (dto.getShippingAddress() == null) throw new MerchantException("Shipping address cannot be null", "INVALID_SHIPPING_ADDRESS_ID");
        if (dto.getOrderItems() == null || dto.getOrderItems().isEmpty()) throw new MerchantException("Order must have at least one item", "INVALID_ORDER_ITEMS");
        if (dto.getCustomerId() == null && (dto.getCustomerEmail() == null || dto.getCustomerEmail().trim().isEmpty())) {
            throw new MerchantException("Customer email is required when customerId is not provided", "INVALID_CUSTOMER_EMAIL");
        }
    }
}