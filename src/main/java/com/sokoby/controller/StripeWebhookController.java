package com.sokoby.controller;

import com.sokoby.repository.OrderRepository;
import com.sokoby.repository.PaymentRepository;
import com.sokoby.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import com.stripe.model.checkout.Session;
import java.util.UUID;
import com.sokoby.entity.Order;
import com.sokoby.entity.Payment;
import com.sokoby.exception.MerchantException;
import com.sokoby.enums.PaymentStatus;

@RestController
@RequestMapping("/payment/webhook")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    paymentService.confirmPayment(session.getId());
                }
            }

            return ResponseEntity.ok("Webhook received");
        } catch (SignatureVerificationException e) {
            logger.error("Invalid webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }
    }
}
