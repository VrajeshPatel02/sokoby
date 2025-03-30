package com.sokoby.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sokoby.controller.StripeWebhookController;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    @Autowired
    private PaymentService paymentService;

    public void handleCheckoutSessionCompleted(Event event, String payload) {
        try {
            // First try using EventDataObjectDeserializer
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            if (deserializer != null && deserializer.getObject().isPresent()) {
                Session session = (Session) deserializer.getObject().get();
                logger.info("Successfully deserialized Session object with ID: {}", session.getId());
                paymentService.confirmPayment(session.getId());
                logger.info("Payment confirmed for session: {}", session.getId());
                return;
            }

            logger.warn("Could not deserialize Session object. Trying direct JSON parsing.");

            // Fall back to manual JSON parsing
            JsonObject jsonEvent = JsonParser.parseString(payload).getAsJsonObject();
            JsonObject data = jsonEvent.getAsJsonObject("data");
            JsonObject object = data.getAsJsonObject("object");
            String sessionId = object.get("id").getAsString();

            logger.info("Extracted session ID from raw JSON: {}", sessionId);
            paymentService.confirmPayment(sessionId);
            logger.info("Payment confirmed for session (from raw JSON): {}", sessionId);
        } catch (Exception e) {
            logger.error("Error in handleCheckoutSessionCompleted: {}", e.getMessage(), e);
            throw e;
        }
    }

    public static void handlePaymentIntentSucceeded(Event event, String payload) {
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

            String failureMessage = "Unknown error";
            if (object.has("last_payment_error") && !object.get("last_payment_error").isJsonNull()) {
                JsonObject error = object.getAsJsonObject("last_payment_error");
                if (error.has("message") && !error.get("message").isJsonNull()) {
                    failureMessage = error.get("message").getAsString();
                }
            }

            logger.info("Payment failed for payment intent (from JSON): {} with error: {}",
                    paymentIntentId, failureMessage);

            // Handle the failed payment through your service
            paymentService.handleFailedPayment(paymentIntentId, failureMessage, null);
        } catch (Exception e) {
            logger.error("Error handling payment_intent.payment_failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
