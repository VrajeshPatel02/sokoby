package com.sokoby.controller;

import com.sokoby.payload.SubscriptionDto;
import com.sokoby.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}