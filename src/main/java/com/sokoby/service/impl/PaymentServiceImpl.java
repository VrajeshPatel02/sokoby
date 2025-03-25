package com.sokoby.service.impl;

import com.sokoby.entity.Order;
import com.sokoby.entity.Payment;
import com.sokoby.enums.PaymentStatus;
import com.sokoby.exception.MerchantException;
import com.sokoby.payload.PaymentDto;
import com.sokoby.payload.PaymentIntentDto;
import com.sokoby.repository.OrderRepository;
import com.sokoby.repository.PaymentRepository;
import com.sokoby.mapper.PaymentMapper;
import com.sokoby.service.PaymentService;
import com.sokoby.service.StripeService;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StripeService stripeService;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository,
                              StripeService stripeService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.stripeService = stripeService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "payments", allEntries = true) // Simplified cache eviction
    public PaymentDto createPayment(PaymentDto dto) {
        validatePaymentInput(dto);

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

        if (paymentRepository.findByOrderId(dto.getOrderId()).isPresent()) {
            throw new MerchantException("Payment already exists for this order", "DUPLICATE_PAYMENT");
        }

        try {
            // Create Stripe Payment Intent
            PaymentIntentDto paymentIntent = stripeService.createPaymentIntent(
                    Long.parseLong(String.valueOf(dto.getAmount())),
                    dto.getCurrency(), // or get from dto
                    "Payment for Order #" + order.getId());

            // Create local payment record
            Payment payment = PaymentMapper.toEntity(dto);
            payment.setOrder(order);
            payment.setStripePaymentId(paymentIntent.getPaymentIntentId());
            payment.setStatus(PaymentStatus.PENDING);

            Payment savedPayment = paymentRepository.save(payment);

            PaymentDto responseDto = PaymentMapper.toDto(savedPayment);
            responseDto.setStripePaymentId(paymentIntent.getPaymentIntentId());

            logger.info("Created payment for order {} with Stripe PaymentIntent {}",
                    order.getId(), paymentIntent.getPaymentIntentId());

            return responseDto;
        } catch (Exception e) {
            logger.error("Failed to create payment for order {}: {}", order.getId(), e.getMessage());
            throw new MerchantException("Failed to create payment", "PAYMENT_CREATION_ERROR");
        }
    }

    @Override
    @Cacheable(value = "payments", key = "#id")
    public PaymentDto getPaymentById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Payment not found", "PAYMENT_NOT_FOUND"));
        logger.info("Retrieved payment with ID: {}", id);
        return PaymentMapper.toDto(payment);
    }

    @Override
    public PaymentDto getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new MerchantException("Payment not found for order", "PAYMENT_NOT_FOUND"));
        logger.info("Retrieved payment for order ID: {}", orderId);
        return PaymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentDto> getPaymentsByCustomerId(UUID customerId) {
        if (!orderRepository.existsByCustomerId(customerId)) {
            throw new MerchantException("No orders found for customer", "CUSTOMER_NOT_FOUND");
        }
        List<Payment> payments = paymentRepository.findByOrder_CustomerId(customerId);
        if (payments.isEmpty()) {
            logger.warn("No payments found for customer ID: {}", customerId);
        }
        return payments.stream().map(PaymentMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<PaymentDto> getPaymentsByCustomerId(UUID customerId, Pageable pageable) {
        if (!orderRepository.existsByCustomerId(customerId)) {
            throw new MerchantException("No orders found for customer", "CUSTOMER_NOT_FOUND");
        }
        Page<Payment> paymentPage = paymentRepository.findByOrder_CustomerId(customerId, pageable);
        if (paymentPage.isEmpty()) {
            logger.warn("No payments found for customer ID: {}", customerId);
        }
        return mapToDtoPage(paymentPage);
    }

    @Override
    public List<PaymentDto> getPaymentsByStoreId(UUID storeId) {
        if (!orderRepository.existsByStoreId(storeId)) {
            throw new MerchantException("No orders found for store", "STORE_NOT_FOUND");
        }
        List<Payment> payments = paymentRepository.findByOrder_StoreId(storeId);
        if (payments.isEmpty()) {
            logger.warn("No payments found for store ID: {}", storeId);
        }
        return payments.stream().map(PaymentMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<PaymentDto> getPaymentsByStoreId(UUID storeId, Pageable pageable) {
        if (!orderRepository.existsByStoreId(storeId)) {
            throw new MerchantException("No orders found for store", "STORE_NOT_FOUND");
        }
        Page<Payment> paymentPage = paymentRepository.findByOrder_StoreId(storeId, pageable);
        if (paymentPage.isEmpty()) {
            logger.warn("No payments found for store ID: {}", storeId);
        }
        return mapToDtoPage(paymentPage);
    }

    private Page<PaymentDto> mapToDtoPage(Page<Payment> paymentPage) {
        List<PaymentDto> dtos = paymentPage.getContent()
                .stream()
                .map(PaymentMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, paymentPage.getPageable(), paymentPage.getTotalElements());
    }

    @Override
    @Transactional
    @CacheEvict(value = "payments", key = "#id")
    public PaymentDto updatePayment(UUID id, PaymentDto dto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Payment not found", "PAYMENT_NOT_FOUND"));

        validatePaymentInput(dto);

        if (!dto.getOrderId().equals(payment.getOrder().getId())) {
            throw new MerchantException("Cannot change order of payment", "INVALID_ORDER_CHANGE");
        }

        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setStatus(PaymentStatus.valueOf(dto.getStatus()));
        payment.setTransactionId(dto.getTransactionId());

        try {
            Payment updatedPayment = paymentRepository.save(payment);
            logger.info("Updated payment with ID: {}", id);
            return PaymentMapper.toDto(updatedPayment);
        } catch (Exception e) {
            logger.error("Failed to update payment with ID: {}", id, e);
            throw new MerchantException("Failed to update payment", "PAYMENT_UPDATE_ERROR");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "payments", key = "#id")
    public void deletePayment(UUID id) {
        if (!paymentRepository.existsById(id)) {
            throw new MerchantException("Payment not found", "PAYMENT_NOT_FOUND");
        }
        try {
            paymentRepository.deleteById(id);
            logger.info("Deleted payment with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete payment with ID: {}", id, e);
            throw new MerchantException("Failed to delete payment", "PAYMENT_DELETION_ERROR");
        }
    }

    @Transactional
    @Override
    public PaymentDto confirmPayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentId(paymentIntentId)
                .orElseThrow(() -> new MerchantException("Payment not found", "PAYMENT_NOT_FOUND"));

        try {
            // Confirm payment with Stripe
            PaymentIntent confirmedIntent = stripeService.confirmPayment(paymentIntentId);

            // Update local payment record
            payment.setStatus(PaymentStatus.valueOf(confirmedIntent.getStatus()));

            Payment updatedPayment = paymentRepository.save(payment);
            logger.info("Confirmed payment for PaymentIntent: {}", paymentIntentId);

            return PaymentMapper.toDto(updatedPayment);
        } catch (Exception e) {
            logger.error("Failed to confirm payment: {}", e.getMessage());
            throw new MerchantException("Failed to confirm payment", "PAYMENT_CONFIRMATION_ERROR");
        }
    }

    @Override
    public PaymentDto cancelPayment(String paymentIntentId) {
        PaymentIntent paymentIntent = stripeService.cancelPayment(paymentIntentId);
        return new PaymentDto(paymentIntent.getId(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency(),
                paymentIntent.getClientSecret(),
                paymentIntent.getStatus(),
                paymentIntent.getDescription());
    }

    private void validatePaymentInput(PaymentDto dto) {
        if (dto.getOrderId() == null) {
            throw new MerchantException("Order ID cannot be null", "INVALID_ORDER_ID");
        }
        if (dto.getAmount() == null || dto.getAmount() < 0) {
            throw new MerchantException("Amount must be non-negative", "INVALID_AMOUNT");
        }
        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().trim().isEmpty()) {
            throw new MerchantException("Payment method cannot be null or empty", "INVALID_PAYMENT_METHOD");
        }
        if (dto.getStatus() == null || dto.getStatus().trim().isEmpty()) {
            throw new MerchantException("Status cannot be null or empty", "INVALID_STATUS");
        }
    }
}