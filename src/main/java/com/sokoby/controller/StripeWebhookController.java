package com.sokoby.controller;

import com.sokoby.service.WebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment/webhook")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Autowired
    private WebhookService webhookService;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            logger.info("Received webhook with payload length: {} bytes", payload.length());

            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            logger.info("Event constructed with type: '{}'", event.getType());

            try {
                switch (event.getType()) {
                    case "checkout.session.completed":
                        logger.info("Handling checkout.session.completed event");
                        webhookService.handleCheckoutSessionCompleted(event, payload);
                        break;

                    case "payment_intent.succeeded":
                        logger.info("Payment intent succeeded event received");
                        webhookService.handlePaymentIntentSucceeded(event, payload);
                        break;

                    case "payment_intent.payment_failed":
                        logger.info("Payment intent failed event received");
                        webhookService.handlePaymentIntentFailed(event, payload);
                        break;

                    case "customer.subscription.created":
                        logger.info("Handling customer.subscription.created event");
                        webhookService.handleSubscriptionCreated(event, payload);
                        break;

                    case "customer.subscription.updated":
                        logger.info("Handling customer.subscription.updated event");
                        webhookService.handleSubscriptionUpdated(event, payload);
                        break;

                    case "customer.subscription.deleted":
                        logger.info("Handling customer.subscription.deleted event");
                        webhookService.handleSubscriptionDeleted(event, payload);
                        break;

                    case "invoice.payment_failed":
                        logger.info("Handling invoice.payment_failed event");
                        webhookService.handleInvoicePaymentFailed(event, payload);
                        break;

                    case "invoice.payment_succeeded":
                        logger.info("Handling invoice.payment_succeeded event");
                        webhookService.handleInvoicePaymentSucceeded(event, payload);
                        break;

                    default:
                        logger.info("Unhandled event type: '{}'", event.getType());
                        break;
                }
            } catch (Exception e) {
                logger.error("Error processing event of type {}: {}", event.getType(), e.getMessage(), e);
                return ResponseEntity.ok("Event received but error during processing: " + e.getMessage());
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (SignatureVerificationException e) {
            logger.error("Invalid webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }

}