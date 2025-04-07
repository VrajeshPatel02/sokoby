package com.sokoby.controller;

import com.sokoby.payload.SubscriptionDto;
import com.sokoby.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> createSubscription(@RequestBody SubscriptionDto subscriptionDto) {
        try {
            logger.info("Received request to create subscription for merchant: {}", subscriptionDto.getMerchant());
            String sessionUrl = paymentService.createSubscriptionSession(subscriptionDto);
            logger.info("Subscription session created successfully, URL: {}", sessionUrl);
            return ResponseEntity.ok(sessionUrl);
        } catch (Exception e) {
            logger.error("Error creating subscription session: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error creating subscription: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubscription(@PathVariable UUID id, @RequestBody SubscriptionDto subscriptionDto) {
        try {
            logger.info("Updating subscription with ID: {}", id);
            SubscriptionDto updatedSubscription = paymentService.updateSubscription(id, subscriptionDto);
            return ResponseEntity.ok(updatedSubscription);
        } catch (Exception e) {
            logger.error("Error updating subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating subscription: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelSubscription(@PathVariable UUID id, @RequestParam(defaultValue = "true") boolean immediate) {
        try {
            logger.info("Canceling subscription with ID: {}", id);
            paymentService.cancelSubscription(id, immediate);
            return ResponseEntity.ok("Subscription canceled successfully");
        } catch (Exception e) {
            logger.error("Error canceling subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error canceling subscription: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubscriptionById(UUID id){
        SubscriptionDto subscriptionById = paymentService.getSubscriptionById(id);
        return ResponseEntity.ok(subscriptionById);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<SubscriptionDto>> getAllSubscriptions(){
        return ResponseEntity.ok(paymentService.getAllSubscriptions());
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<?> renewSubscription(@PathVariable UUID id) {
        try {
            logger.info("Renewing subscription with ID: {}", id);
            SubscriptionDto renewedSubscription = paymentService.renewSubscription(id);
            return ResponseEntity.ok(renewedSubscription);
        } catch (Exception e) {
            logger.error("Error renewing subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error renewing subscription: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/retry-payment")
    public ResponseEntity<?> retrySubscriptionPayment(@PathVariable UUID id) {
        try {
            logger.info("Retrying payment for subscription with ID: {}", id);
            paymentService.retrySubscriptionPayment(id);
            return ResponseEntity.ok("Payment retry initiated");
        } catch (Exception e) {
            logger.error("Error retrying payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrying payment: " + e.getMessage());
        }
    }
}