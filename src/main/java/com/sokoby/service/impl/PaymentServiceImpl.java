package com.sokoby.service.impl;

import com.sokoby.entity.Merchant;
import com.sokoby.entity.Order;
import com.sokoby.entity.Payment;
import com.sokoby.entity.Subscription;
import com.sokoby.enums.OrderStatus;
import com.sokoby.enums.PaymentStatus;
import com.sokoby.enums.SubscriptionStatus;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.PaymentMapper;
import com.sokoby.mapper.SubscriptionMapper;
import com.sokoby.payload.PaymentDto;
import com.sokoby.payload.SubscriptionDto;
import com.sokoby.repository.MerchantRepository;
import com.sokoby.repository.OrderRepository;
import com.sokoby.repository.PaymentRepository;
import com.sokoby.repository.SubscriptionRepository;
import com.sokoby.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
    public class PaymentServiceImpl implements PaymentService {

        private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

        private final PaymentRepository paymentRepository;
        private final OrderRepository orderRepository;
        private final MerchantRepository merchantRepository;

        @Value("${app.success.url}")
        private String successUrl;

        @Value("${app.cancel.url}")
        private String cancelUrl;

        @Value("${app.subscription.success.url}")
        private String subscriptionSuccessUrl;

        @Value("${app.subscription.cancel.url}")
        private String getSubscriptionCancelUrl;

        private final String stripeSecretKey;
        private final SubscriptionRepository subscriptionRepository;

        @Autowired
        public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository, MerchantRepository merchantRepository,
                                  @Value("${stripe.secret.key}") String stripeSecretKey, SubscriptionRepository subscriptionRepository) {
            this.paymentRepository = paymentRepository;
            this.orderRepository = orderRepository;
            this.merchantRepository = merchantRepository;
            this.stripeSecretKey = stripeSecretKey;
            this.subscriptionRepository = subscriptionRepository;
            logger.info("Stripe secret key injected: {}", stripeSecretKey); // Log for debugging (mask in production)
            if (stripeSecretKey == null || stripeSecretKey.trim().isEmpty()) {
                throw new IllegalArgumentException("Stripe secret key is not configured in application.properties");
            }
            Stripe.apiKey = stripeSecretKey;
            logger.info("Stripe API key set successfully");
        }

        @Transactional
        @Override
        public String createPaymentSession(UUID orderId) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

            try {
                if (Stripe.apiKey == null || Stripe.apiKey.trim().isEmpty()) {
                    Stripe.apiKey = stripeSecretKey; // Re-set if necessary
                    logger.warn("Stripe API key was null; reset to injected value");
                }

                SessionCreateParams params = SessionCreateParams.builder()
                        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(successUrl + order.getStore().getId()+ "/order-success" + "?orderId=" + order.getId())
                        .setCancelUrl(cancelUrl + "?orderId=" + order.getId())
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

                Session session = Session.create(params);
                Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(()->new MerchantException("Order Not Found", "ORDER_NOT_FOUND"));
                payment.setStripePaymentIntentId(session.getPaymentIntent());
                payment.setStripeCheckoutSessionId(session.getId());
                logger.info("Checkout session created for order {}: {}", orderId, session.getUrl());
                return session.getUrl();
            } catch (StripeException e) {
                logger.error("Stripe payment error for order {}: {}", orderId, e.getMessage());
                throw new MerchantException("Payment processing failed: " + e.getMessage(), "PAYMENT_PROCESSING_ERROR");
            }
        }

        @Override
        @Transactional
        public PaymentDto createPayment(UUID orderId) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

            try {
                Payment payment = new Payment();
                payment.setOrder(order);
                payment.setAmount(order.getTotalAmount());
                payment.setStatus(PaymentStatus.valueOf("PENDING".toUpperCase()));

                Payment savedPayment = paymentRepository.save(payment);
                logger.info("Created payment session {} for order {}", savedPayment.getId(), orderId);

                PaymentDto dto = PaymentMapper.toDto(savedPayment);
                return dto;
            } catch (Exception e) {
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

    @Override
    @Transactional
    public PaymentDto confirmPayment(String sessionId) {
        Payment payment = paymentRepository.findByStripeCheckoutSessionId(sessionId)
                .orElseThrow(() -> new MerchantException("Payment not found for session", "PAYMENT_NOT_FOUND"));

        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        Order order = orderRepository.findByPaymentId(payment.getId()).orElseThrow(() -> new MerchantException("Order not found for payment", "ORDER_NOT_FOUND"));
        order.setStatus(OrderStatus.PLACED);
        logger.info("Payment confirmed for session {}", sessionId);

        return PaymentMapper.toDto(payment);
    }

    @Transactional
    @Override
    public void handleFailedPayment(String paymentIntentId, String errorMessage, Map<String, String> metadata) {
        logger.info("Handling failed payment for payment intent: {}", paymentIntentId);

        // Find payment by payment intent ID
        Optional<Payment> paymentOpt = paymentRepository.findByStripePaymentIntentId(paymentIntentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(errorMessage);
            paymentRepository.save(payment);

            // Update order status
            if (payment.getOrder() != null) {
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                orderRepository.save(order);

            }

            logger.info("Payment marked as failed for payment intent: {}", paymentIntentId);
        } else if (metadata != null && metadata.containsKey("order_id")) {
            // If payment not found but we have order info in metadata
            String orderId = metadata.get("order_id");
            Optional<Order> orderOpt = orderRepository.findById(UUID.fromString(orderId));

            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                orderRepository.save(order);

                // Create a new payment record for the failed attempt
                Payment payment = new Payment();
                payment.setOrder(order);
                payment.setAmount(order.getTotalAmount());
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage(errorMessage);
                paymentRepository.save(payment);
            }
        } else {
            logger.warn("Could not find payment or order for failed payment intent: {}", paymentIntentId);
        }
    }
        @Transactional
        @Override
        public String createSubscriptionSession(SubscriptionDto subscriptionDto) {
            Merchant merchant = merchantRepository.findById(subscriptionDto.getMerchant())
                    .orElseThrow(() -> new MerchantException("Merchant Not found", "MERCHANT_NOT_FOUND"));
            try {
                if (Stripe.apiKey == null || Stripe.apiKey.trim().isEmpty()) {
                    Stripe.apiKey = stripeSecretKey;
                    logger.warn("Stripe API key was null; reset to injected value");
                }

                SessionCreateParams params = SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                        .setSuccessUrl(subscriptionSuccessUrl)
                        .setCancelUrl(subscriptionSuccessUrl)
                        .setSubscriptionData(SessionCreateParams.SubscriptionData.builder().setTrialPeriodDays(14L).build())
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount((long) (subscriptionDto.getAmount() * 100))
                                                        .setRecurring(SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                                                .setInterval(Objects.equals(subscriptionDto.getInterval(), "YEAR") ? SessionCreateParams.LineItem.PriceData.Recurring.Interval.YEAR : SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                                                .build())
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Merchant #" + subscriptionDto.getMerchant())
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();

                Session session = Session.create(params);
                Subscription subscription = SubscriptionMapper.toEntity(subscriptionDto);
                subscription.setMerchant(merchant);
                subscription.setStatus(SubscriptionStatus.PENDING);
                subscription.setStripeCheckoutSessionId(session.getId());
                subscriptionRepository.save(subscription);

                Payment payment = new Payment();
                payment.setStatus(PaymentStatus.PENDING);
                payment.setStripeCheckoutSessionId(session.getId());
                payment.setAmount(subscriptionDto.getAmount());
                paymentRepository.save(payment);
                logger.info("Subscription session created with ID: {}", session.getId());
                return session.getUrl();
            } catch (StripeException e) {
                throw new MerchantException("Payment processing failed: " + e.getMessage(), "PAYMENT_PROCESSING_ERROR");
            }
        }

    @Transactional
    @Override
    public SubscriptionDto updateSubscription(UUID subscriptionId, SubscriptionDto subscriptionDto) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new MerchantException("Subscription not found", "SUBSCRIPTION_NOT_FOUND"));

        try {
            // Retrieve the current Stripe subscription
            com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId());

            // Create a new Price object for the updated amount and interval
            Map<String, Object> priceParams = new HashMap<>();
            priceParams.put("unit_amount", (long) (subscriptionDto.getAmount() * 100));
            priceParams.put("currency", "usd");
            priceParams.put("recurring", Map.of(
                    "interval", Objects.equals(subscriptionDto.getInterval(), "YEAR") ? "year" : "month"
            ));
            priceParams.put("product_data", Map.of(
                    "name", "Merchant #" + subscriptionDto.getMerchant()
            ));
            Price price = Price.create(priceParams);

            // Update the subscription with the new price
            Map<String, Object> updateParams = new HashMap<>();
            updateParams.put("items", List.of(Map.of(
                    "id", stripeSubscription.getItems().getData().get(0).getId(),
                    "price", price.getId() // Use the new price ID
            )));
            updateParams.put("proration_behavior", "create_prorations"); // Adjust billing for mid-cycle changes
            stripeSubscription.update(updateParams);

            // Sync local subscription entity
            subscription.setAmount(subscriptionDto.getAmount());
            subscription.setInterval(subscriptionDto.getInterval());
            subscriptionRepository.save(subscription);

            logger.info("Subscription {} updated with new amount {} and interval {}", subscriptionId, subscriptionDto.getAmount(), subscriptionDto.getInterval());
            return SubscriptionMapper.toDto(subscription); // Assuming @Autowired SubscriptionMapper subscriptionMapper
        } catch (StripeException e) {
            logger.error("Failed to update Stripe subscription {}: {}", subscriptionId, e.getMessage(), e);
            throw new MerchantException("Failed to update subscription: " + e.getMessage(), "SUBSCRIPTION_UPDATE_ERROR");
        }
    }

    @Transactional
    @Override
    public void cancelSubscription(UUID subscriptionId, boolean immediate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new MerchantException("Subscription not found", "SUBSCRIPTION_NOT_FOUND"));

        try {
            com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId());
            if (immediate) {
                stripeSubscription.cancel();
                subscription.setStatus(SubscriptionStatus.CANCELED);
            } else {
                Map<String, Object> params = new HashMap<>();
                params.put("cancel_at_period_end", true);
                stripeSubscription.update(params);
                subscription.setStatus(SubscriptionStatus.PENDING_CANCELLATION); // Optional custom status
            }
            subscriptionRepository.save(subscription);
        } catch (StripeException e) {
            throw new MerchantException("Failed to cancel subscription: " + e.getMessage(), "SUBSCRIPTION_CANCEL_ERROR");
        }
    }

    @Override
    public SubscriptionDto getSubscriptionById(UUID id) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new MerchantException("Subscription not found by id" + id, "SUBSCRIPTION_NOT_FOUND"));
        return SubscriptionMapper.toDto(subscription);
    }

    @Override
    public List<SubscriptionDto> getAllSubscriptions() {
        try {
            List<Subscription> subscriptions = subscriptionRepository.findAll();
            return subscriptions.stream().map(SubscriptionMapper::toDto).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public SubscriptionDto renewSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new MerchantException("Subscription not found", "SUBSCRIPTION_NOT_FOUND"));

        try {
            com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId());
            if (stripeSubscription.getCancelAtPeriodEnd()) {
                Map<String, Object> params = new HashMap<>();
                params.put("cancel_at_period_end", false);
                stripeSubscription.update(params);

                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(subscription);
                logger.info("Subscription {} reactivated", subscriptionId);
                return SubscriptionMapper.toDto(subscription);
            } else {
                throw new MerchantException("Subscription is not set to cancel at period end", "SUBSCRIPTION_NOT_PENDING_CANCELLATION");
            }
        } catch (StripeException e) {
            throw new MerchantException("Failed to renew subscription: " + e.getMessage(), "SUBSCRIPTION_RENEW_ERROR");
        }
    }

    @Override
    public void retrySubscriptionPayment(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new MerchantException("Subscription not found", "SUBSCRIPTION_NOT_FOUND"));

        try {
            com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId());
            if ("past_due".equals(stripeSubscription.getStatus()) || "unpaid".equals(stripeSubscription.getStatus())) {
                // Fetch the latest invoice
                Invoice invoice = Invoice.list(Map.of("subscription", subscription.getStripeSubscriptionId(), "limit", 1))
                        .getData().get(0);
                invoice.pay();

                // Payment success triggers invoice.payment_succeeded webhook
                logger.info("Retried payment for subscription {}", subscriptionId);
            } else {
                logger.info("Subscription {} is not in a state requiring payment retry: {}", subscriptionId, stripeSubscription.getStatus());
            }
        } catch (StripeException e) {
            throw new MerchantException("Failed to retry payment: " + e.getMessage(), "PAYMENT_RETRY_ERROR");
        }
    }
}