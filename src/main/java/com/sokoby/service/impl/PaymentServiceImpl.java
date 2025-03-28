package com.sokoby.service.impl;

import com.sokoby.entity.Order;
import com.sokoby.entity.Payment;
import com.sokoby.enums.PaymentStatus;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.PaymentMapper;
import com.sokoby.payload.PaymentDto;
import com.sokoby.repository.OrderRepository;
import com.sokoby.repository.PaymentRepository;
import com.sokoby.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.success.url}") // e.g., http://localhost:8080/payment/success
    private String successUrl;

    @Value("${app.cancel.url}") // e.g., http://localhost:8080/payment/cancel
    private String cancelUrl;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        Stripe.apiKey = stripeSecretKey; // Platform's secret key
    }

    @Override
    @Transactional
    public PaymentDto createPayment(UUID orderId, String paymentMethodId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

        if (order.getStore().getStripeAccountId() == null) {
            throw new MerchantException("Store has no payment gateway configured", "NO_PAYMENT_GATEWAY");
        }

        try {
            // Create Stripe Checkout Session
            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD) // Debit/Credit Cards
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.PAYPAL) // PayPal if enabled
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?orderId=" + orderId)
                    .setCancelUrl(cancelUrl + "?orderId=" + orderId)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount((long) (order.getTotalAmount() * 100))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + order.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            Session session = Session.create(params,
                    com.stripe.net.RequestOptions.builder()
                            .setStripeAccount(order.getStore().getStripeAccountId())
                            .build());

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setStripeCheckoutSessionId(session.getId());
            payment.setStatus(PaymentStatus.valueOf("pending")); // Initial status; updated via webhook

            Payment savedPayment = paymentRepository.save(payment);
            logger.info("Created payment session {} for order {}", savedPayment.getId(), orderId);

            PaymentDto dto = PaymentMapper.toDto(savedPayment);
            dto.setStripeCheckoutSessionId(session.getUrl()); // Return URL for redirection
            return dto;
        } catch (StripeException e) {
            logger.error("Stripe payment error for order {}: {}", orderId, e.getMessage());
            throw new MerchantException("Payment processing failed: " + e.getMessage(), "PAYMENT_PROCESSING_ERROR");
        }
    }

    @Override
    public PaymentDto getPaymentById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Payment not found", "PAYMENT_NOT_FOUND"));
        logger.info("Retrieved payment with ID: {}", id);
        return PaymentMapper.toDto(payment);
    }

    @Override
    public PaymentDto getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new MerchantException("No payment found for order: " + orderId, "PAYMENT_NOT_FOUND"));
        logger.info("Retrieved payment for order ID: {}", orderId);
        return PaymentMapper.toDto(payment);
    }
}