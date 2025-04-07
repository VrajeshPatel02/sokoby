package com.sokoby.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sokoby.controller.StripeWebhookController;
import com.sokoby.entity.Payment;
import com.sokoby.entity.Subscription;
import com.sokoby.enums.SubscriptionStatus;
import com.sokoby.exception.MerchantException;
import com.sokoby.repository.PaymentRepository;
import com.sokoby.repository.SubscriptionRepository;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sokoby.enums.PaymentStatus;

import java.util.Optional;

@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Transactional
    public void handleCheckoutSessionCompleted(Event event, String payload) {
        try {
            // Fall back to manual JSON parsing
            JsonObject jsonEvent = JsonParser.parseString(payload).getAsJsonObject();
            JsonObject data = jsonEvent.getAsJsonObject("data");
            JsonObject object = data.getAsJsonObject("object");
            String sessionId = object.get("id").getAsString();
            String mode = object.get("mode").getAsString();

            if ("subscription".equals(mode)) {
                // Handle subscription
                String subscriptionId = object.get("subscription").getAsString();
                Subscription subscription = subscriptionRepository.findByStripeCheckoutSessionId(sessionId)
                        .orElseThrow(() -> new MerchantException("Subscription not found for session", "SUBSCRIPTION_NOT_FOUND"));
                subscription.setStripeSubscriptionId(subscriptionId);
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(subscription);

                Payment payment = paymentRepository.findByStripeCheckoutSessionId(sessionId)
                        .orElseThrow(() -> new MerchantException("Subscription not found for session", "SUBSCRIPTION_NOT_FOUND"));
                payment.setStripeSubscriptionId(subscriptionId);
                payment.setStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);

                logger.info("Subscription {} created from checkout session {} (from JSON)", subscriptionId, sessionId);
            } else {
                // Handle payment
                paymentService.confirmPayment(sessionId);
                logger.info("Payment confirmed for session (from JSON): {}", sessionId);
            }
        } catch (Exception e) {
            logger.error("Error in handleCheckoutSessionCompleted: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void handlePaymentIntentSucceeded(Event event, String payload) {
        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            if (deserializer != null && deserializer.getObject().isPresent()) {
                PaymentIntent paymentIntent = (PaymentIntent) deserializer.getObject().get();
                logger.info("Payment succeeded for payment intent: {}", paymentIntent.getId());
                // Add your payment success handling logic here
                return;
            }

            // Fall back to JSON parsing
            JsonObject jsonEvent = JsonParser.parseString(payload).getAsJsonObject();
            JsonObject object = jsonEvent.getAsJsonObject("data").getAsJsonObject("object");
            String paymentIntentId = object.get("id").getAsString();

            logger.info("Payment succeeded for payment intent (from JSON): {}", paymentIntentId);
            // Add your payment success handling logic here
        } catch (Exception e) {
            logger.error("Error handling payment_intent.succeeded: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void handlePaymentIntentFailed(Event event, String payload) {
        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            if (deserializer != null && deserializer.getObject().isPresent()) {
                PaymentIntent paymentIntent = (PaymentIntent) deserializer.getObject().get();
                String failureMessage = paymentIntent.getLastPaymentError() != null ?
                        paymentIntent.getLastPaymentError().getMessage() : "Unknown error";

                logger.info("Payment failed for payment intent: {} with error: {}",
                        paymentIntent.getId(), failureMessage);
                return;
            }

            // Fall back to JSON parsing
            JsonObject jsonEvent = JsonParser.parseString(payload).getAsJsonObject();
            JsonObject object = jsonEvent.getAsJsonObject("data").getAsJsonObject("object");
            String paymentIntentId = object.get("id").getAsString();
            Payment payment = paymentRepository.findByStripeCheckoutSessionId(paymentIntentId)
                    .orElseThrow(() -> new MerchantException("Payment not found by payment intent", "PAYMENT_NOT_FOUND"));

            String failureMessage = "Unknown error";
            if (object.has("last_payment_error") && !object.get("last_payment_error").isJsonNull()) {
                JsonObject error = object.getAsJsonObject("last_payment_error");
                if (error.has("message") && !error.get("message").isJsonNull()) {
                    failureMessage = error.get("message").getAsString();
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setErrorMessage(failureMessage);
                }
            }
            paymentRepository.save(payment);

            logger.info("Payment failed for payment intent (from JSON): {} with error: {}",
                    paymentIntentId, failureMessage);

            // Handle the failed payment through your service
            paymentService.handleFailedPayment(paymentIntentId, failureMessage, null);
        } catch (Exception e) {
            logger.error("Error handling payment_intent.payment_failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void handleInvoicePaymentSucceeded(Event event, String payload) {
        try {
            JsonObject jsonEvent = JsonParser.parseString(payload).getAsJsonObject();
            JsonObject object = jsonEvent.getAsJsonObject("data").getAsJsonObject("object");
            String subscriptionId = object.get("subscription").getAsString();

            Optional<Subscription> subscriptionOptional = subscriptionRepository.findByStripeSubscriptionId(subscriptionId);
            if (subscriptionOptional.isPresent()) {
                Subscription subscription = subscriptionOptional.get();
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(subscription);

                Payment payment = paymentRepository.findByStripeSubscriptionId(subscriptionId)
                        .orElseGet(() -> {
                            Payment newPayment = new Payment();
                            newPayment.setStripeSubscriptionId(subscriptionId);
                            return newPayment;
                        });
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setErrorMessage(null);
                paymentRepository.save(payment);

                logger.info("Payment succeeded for subscription {}", subscriptionId);
            }
        } catch (Exception e) {
            logger.error("Error handling invoice.payment_succeeded: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void handleSubscriptionCreated(Event event, String payload) {
        try {
            JsonObject jsonEvent = JsonParser.parseString(payload).getAsJsonObject();
            JsonObject object = jsonEvent.getAsJsonObject("data").getAsJsonObject("object");
            String sessionId = object.get("id").getAsString();
            String subscriptionId = object.get("subscription").getAsString();

            Optional<Subscription> subscriptionOptional = subscriptionRepository.findByStripeCheckoutSessionId(sessionId);
            Subscription subscription;
            if (subscriptionOptional.isPresent()) {
                subscription = subscriptionOptional.get();
            } else {
                subscription = new Subscription();
                subscription.setStripeSubscriptionId(subscriptionId);
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                // Merchant might need to be set via metadata or another mechanism
            }

            subscription.setStripeSubscriptionId(subscriptionId);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(subscription);
            Payment payment = paymentRepository.findByStripeCheckoutSessionId(sessionId)
                    .orElseThrow(() -> new MerchantException("Subscription not found for session", "SUBSCRIPTION_NOT_FOUND"));
            payment.setStripeSubscriptionId(subscriptionId);
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            logger.info("Subscription {} created successfully (from JSON)", subscriptionId);
        } catch (Exception e) {
            logger.error("Error handling customer.subscription.created: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void handleSubscriptionUpdated(Event event, String payload) {
        try {
            // Use JSON parsing only
            JsonObject jsonEvent = JsonParser.parseString(payload).getAsJsonObject();
            JsonObject object = jsonEvent.getAsJsonObject("data").getAsJsonObject("object");
            String subscriptionId = object.get("id").getAsString();
            String status = object.get("status").getAsString();

            Optional<Subscription> subscriptionOptional= subscriptionRepository.findByStripeCheckoutSessionId(subscriptionId);
            if (subscriptionOptional.isPresent()) {
                Payment newPayment = new Payment();
                newPayment.setStripeSubscriptionId(subscriptionId);
                // Set initial status based on the event (could be refined)
                newPayment.setStatus(PaymentStatus.SUCCESS);
                switch (status) {
                    case "active":
                    case "trialing":
                        newPayment.setStatus(PaymentStatus.SUCCESS);
                        break;
                    case "past_due":
                    case "unpaid":
                        newPayment.setStatus(PaymentStatus.FAILED);
                        newPayment.setErrorMessage("Subscription payment failed: " + status);
                        break;
                    case "canceled":
                        newPayment.setStatus(PaymentStatus.CANCELED);
                        break;
                    default:
                        logger.warn("Unhandled subscription status: {}", status);
                        return;

                }
                paymentRepository.save(newPayment);
            }
            logger.info("Subscription {} updated to status: {} (from JSON)", subscriptionId, status);
        } catch (Exception e) {
            logger.error("Error handling customer.subscription.updated: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void handleSubscriptionDeleted(Event event, String payload) {
        try {
            // Use JSON parsing only
            JsonObject jsonEvent = JsonParser.parseString(payload).getAsJsonObject();
            JsonObject object = jsonEvent.getAsJsonObject("data").getAsJsonObject("object");
            String subscriptionId = object.get("id").getAsString();

            Optional<Subscription> subscriptionOptional = subscriptionRepository.findByStripeCheckoutSessionId(subscriptionId);
            if (subscriptionOptional.isPresent()) {
                Payment newPayment = new Payment();
                newPayment.setStripeSubscriptionId(subscriptionId);
                newPayment.setStatus(PaymentStatus.CANCELED);
                paymentRepository.save(newPayment);
            }
            logger.info("Subscription {} deleted (from JSON)", subscriptionId);
        } catch (Exception e) {
            logger.error("Error handling customer.subscription.deleted: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void handleInvoicePaymentFailed(Event event, String payload) {
        try {
            JsonObject jsonEvent = JsonParser.parseString(payload).getAsJsonObject();
            JsonObject object = jsonEvent.getAsJsonObject("data").getAsJsonObject("object");
            String subscriptionId = object.get("subscription").getAsString();
            String attemptCount = object.get("attempt_count").getAsString();
            String failureReason = object.has("last_payment_error") && !object.get("last_payment_error").isJsonNull()
                    ? object.getAsJsonObject("last_payment_error").get("message").getAsString()
                    : "Unknown error";

            Optional<Subscription> subscriptionOptional = subscriptionRepository.findByStripeSubscriptionId(subscriptionId);
            if (subscriptionOptional.isPresent()) {
                Subscription subscription = subscriptionOptional.get();
                Payment payment = paymentRepository.findByStripeSubscriptionId(subscriptionId)
                        .orElseGet(() -> {
                            Payment newPayment = new Payment();
                            newPayment.setStripeSubscriptionId(subscriptionId);
                            return newPayment;
                        });
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage("Payment failed after " + attemptCount + " attempts: " + failureReason);
                paymentRepository.save(payment);

                // Optional: Notify customer via email or UI
                logger.info("Payment failed for subscription {} after {} attempts: {}", subscriptionId, attemptCount, failureReason);
            } else {
                logger.warn("No subscription found for Stripe subscription ID: {}", subscriptionId);
            }
        } catch (Exception e) {
            logger.error("Error handling invoice.payment_failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}